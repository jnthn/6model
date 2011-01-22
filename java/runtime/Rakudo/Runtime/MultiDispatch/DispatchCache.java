package Rakudo.Runtime.MultiDispatch;

import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.SharedTable;

/// <summary>
/// This is a first cut at a multi-dispatch cache that aims to be able
/// to cache on types and repr-definedness, only grow to a maximum size,
/// be relatively cache friendly, be thread safe, be lock-free, and be
/// reasonably fast to do lookups in. I came up with it on a train at 9am
/// so it may not be perfect. :-)
/// 
/// The basic idea is that we start with an array of references to arity
/// specific sub-caches, and each of those is a bunch of type IDs and
/// then a reference to the result that we want to call. We have a limit
/// to the maximum arity of things we cache, and also a limit to the
/// number of things cached per arity. We do random eviction on the
/// latter; things that get called a lot will quickly find their way
/// back into the cache, so probabalistically it should be reasonable.
/// 
/// Additionally, this is implemented in a way that tries to keep things
/// contiguous in memory. We could do better in C I suspect, but this
/// shouldn't be too bad.
/// 
/// Finally, it's a lock-free design, meaning we keep the cache per arity
/// immutable once it's made. To add something new, we snapshot the current,
/// build a new thing and CAS it in place. This means that lookups can
/// safely happen without locking.
/// 
/// An improvement may well be to sort the list of type tuples. We could
/// then know when we won't find anything earlier in lookup. (Binary
/// search is also possible but probably has crappy cache locality.) But
/// for now, keep it simple. :-)
/// </summary>
public class DispatchCache
{
    /// <summary>
    /// Maximum positional arity we cache up to. (Good to make it a
    /// power of 2, minus 1.)
    /// </summary>
    private int MAX_ARITY = 3;  // const int in C#

    /// <summary>
    /// Maximum entries we cache per arity. (Good to make it a
    /// power of 2, minus 1.)
    /// </summary>
    private int MAX_ENTRIES = 15;  // const int in C#

    /// <summary>
    /// This is what we keep per arity.
    /// </summary>
    private class ArityCache
    {
        /// <summary>
        /// This is a bunch of type IDs. We allocate it arity * MAX_ENTRIES
        /// big and go through it in arity sized chunks.
        /// </summary>
        public long[] TypeIDs;

        /// <summary>
        /// This is the result objects array.
        /// </summary>
        public RakudoObject[] Results;

        /// <summary>
        /// The number of entries in the cache.
        /// </summary>
        public int NumEntries;
    }

    /// <summary>
    /// This is our array indexed by arity.
    /// </summary>
    private volatile ArityCache[] ArityCaches;

    /// <summary>
    /// This is our array indexed by arity.
    /// </summary>
    private static java.util.Random RandomGenerator = new java.util.Random();

    /// <summary>
    /// Constructor for a new dispatch cache.
    /// </summary>
    public DispatchCache()
    {
        // Just allocate the arity caches for now - we can do the
        // rest on demand.
        ArityCaches = new ArityCache[MAX_ARITY + 1];
    }

    /// <summary>
    /// Does a cache lookup based on the passed positional arguments.
    /// If a candidate is found, returns it. Otherwise, returns null.
    /// </summary>
    /// <param name="Positionals"></param>
    /// <returns></returns>
    public RakudoObject Lookup(RakudoObject[] Positionals)
    {
        // If it's within the arity we cache...
        if (Positionals.length <= MAX_ARITY)
        {
            // ...and we did cache something...
            ArityCache Cache = ArityCaches[Positionals.length];
            if (Cache.NumEntries != 0)
            {
                // Get what we're looking for.
                long[] Seeking = PositionalsToTypeCacheIDs(Positionals);

                // Look through the cache for it. ci = type cache array index,
                // ri = result list index.
                int ci = 0;
                for (int ri = 0; ri < Cache.NumEntries; ri++)
                {
                    boolean Matched = true;
                    for (int j = 0; j < Positionals.length; j++)
                    {
                        if (Seeking[j] != Cache.TypeIDs[ci])
                        {
                            Matched = false;
                            break;
                        }
                        ci++;
                    }
                    if (Matched)
                        return Cache.Results[ri];
                }
            }
        }
        return null;
    }

    /// <summary>
    /// Adds the given result to the dispatch cache for the provided
    /// positional arguments.
    /// </summary>
    /// <param name="Positionals"></param>
    /// <param name="Result"></param>
    public void Add(RakudoObject[] Positionals, RakudoObject Result)
    {
        // Don't cache things with excessive arity.
        if (Positionals.length <= MAX_ARITY)
        {
            // Compute the type cache ID tuple.
            long[] ToAdd = PositionalsToTypeCacheIDs(Positionals);

            // Snapshot the previous arity cache.
            ArityCache Previous = ArityCaches[Positionals.length];

            // Build a new one.
            ArityCache New = new ArityCache();
            if (Previous == null)
            {
                // First time. We go in slot 0.
                New.NumEntries = 1;
                New.Results = new RakudoObject[MAX_ENTRIES + 1];
                New.TypeIDs = new long[MAX_ENTRIES * Positionals.length];
                for (int i = 0; i < ToAdd.length; i++)
                    New.TypeIDs[i] = ToAdd[i];
                New.Results[0] = Result;
            }
            else
            {
                // Copy existing entries.
                New.NumEntries = Previous.NumEntries;
                New.TypeIDs = (long[])Previous.TypeIDs.clone();
                New.Results = (RakudoObject[])Previous.Results.clone();

                // Space for the new one?
                if (New.NumEntries <= MAX_ENTRIES)
                {
                    // We can go on the end.
                    int i, j;
                    for (i = 0, j = New.NumEntries * ToAdd.length; i < ToAdd.length; i++, j++)
                        New.TypeIDs[j] = ToAdd[i];
                    New.Results[New.NumEntries] = Result;
                    New.NumEntries++;
                }
                else
                {
                    // Pick a victim.
                    int Evictee = RandomGenerator.nextInt(MAX_ENTRIES + 1);
                    int i, j;
                    for (i = 0, j = Evictee * ToAdd.length; i < ToAdd.length; i++, j++)
                        New.TypeIDs[j] = ToAdd[i];
                    New.Results[Evictee] = Result;
                }
            }

            // Pop it in place, if nothing beat us to it. Otherwise,
            // we let whatever slipped in first win. (We may find it is
            // also beneficial to loop here and try to add this entry
            // again, but may be too much churn, and if it a given combination
            // is called a lot, it'll make it in at some point.)
            synchronized(this) {
                if (ArityCaches[ToAdd.length] == Previous) {
                    ArityCaches[ToAdd.length] = New;
                }
            }
            // The above replaces the following C#, which should be more
            // performant in multithreaded programs.
            // Interlocked.CompareExchange<ArityCache>(ref ArityCaches[ToAdd.Length], New, Previous);
            // See: http://www.ibm.com/developerworks/java/library/j-jtp10264
            // and http://javamex.com/tutorials/synchronization_concurrency_9_locks_j5.shtml
            // and http://download.oracle.com/javase/1.5.0/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.WriteLock.html
        }
    }

    /// <summary>
    /// Takes a set of positional parameters and, based on their STable
    /// IDs and definedness, 
    /// </summary>
    /// <param name="Positionals"></param>
    /// <returns></returns>
    private long[] PositionalsToTypeCacheIDs(RakudoObject[] Positionals)
    {
        long[] Result = new long[Positionals.length];
        for (int i = 0; i < Positionals.length; i++)
        {
            SharedTable sTable = Positionals[i].getSTable();
            Result[i] = sTable.getTypeCacheID() | (sTable.REPR.defined(null, Positionals[i]) ? 1L : 0L);
        }
        return Result;
    }
}

