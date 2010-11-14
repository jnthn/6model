using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime.MultiDispatch
{
    /// <summary>
    /// Very first cut implementation of a multi-dispatcher. Doesn't yet
    /// know about subtyping beyond no type being the top type. Yes, this
    /// will likely get replaced (or extensively re-done) at some point.
    /// </summary>
    public static class MultiDispatcher
    {
        /// <summary>
        /// Represents a node in the multi-dispatch DAG used to do the topological
        /// sort.
        /// </summary>
        private class CandidateGraphNode
        {
            public RakudoCodeRef.Instance Candidate;
            public CandidateGraphNode[] Edges;
            public int EdgesIn;
            public int EdgesOut;
        }

        /// <summary>
        /// Indicates an edge should be removed in the next iteration.
        /// </summary>
        private const int EDGE_REMOVAL_TODO = -1;

        /// <summary>
        /// Indicates that an edge was already removed.
        /// </summary>
        private const int EDGE_REMOVED = -2;

        /// <summary>
        /// Finds the best candidate, if one exists, and returns it.
        /// </summary>
        /// <param name="Candidates"></param>
        /// <param name="Capture"></param>
        /// <returns></returns>
        public static RakudoObject FindBestCandidate(ThreadContext TC, RakudoCodeRef.Instance DispatchRoutine, RakudoObject Capture)
        {
            // Extract the native capture.
            // XXX Handle non-native captures too.
            var NativeCapture = Capture as P6capture.Instance;

            // First, try the dispatch cache.
            if (DispatchRoutine.MultiDispatchCache != null && NativeCapture.Nameds == null)
            {
                var CacheResult = DispatchRoutine.MultiDispatchCache.Lookup(NativeCapture.Positionals);
                if (CacheResult != null)
                    return CacheResult;
            }

            // Sort the candidates.
            // XXX Cache this in the future.
            var SortedCandidates = Sort(TC, DispatchRoutine.Dispatchees);

            // Now go through the sorted candidates and find the first one that
            // matches.
            var PossiblesList = new List<RakudoCodeRef.Instance>();
            foreach (RakudoCodeRef.Instance Candidate in SortedCandidates)
            {
                // If we hit a null, we're at the end of a group.
                if (Candidate == null)
                {
                    if (PossiblesList.Count == 1)
                    {
                        // We have an unambiguous first candidate. Cache if possible and
                        // return it.
                        if (NativeCapture.Nameds == null)
                        {
                            if (DispatchRoutine.MultiDispatchCache == null)
                                DispatchRoutine.MultiDispatchCache = new DispatchCache();
                            DispatchRoutine.MultiDispatchCache.Add(NativeCapture.Positionals, PossiblesList[0]);
                        }
                        return PossiblesList[0];
                    }
                    else if (PossiblesList.Count > 1)
                    {
                        // Here is where you'd handle constraints.
                        throw new Exception("Ambiguous dispatch: more than one candidate matches");
                    }
                    else
                    {
                        continue;
                    }
                }
                
                /* Check if it's admissable by arity. */
                var NumArgs = NativeCapture.Positionals.Length;
                if (NumArgs < Candidate.Sig.NumRequiredPositionals ||
                    NumArgs > Candidate.Sig.NumPositionals)
                    continue;

                /* Check if it's admissable by types and definedness. */
                var TypeCheckCount = Math.Min(NumArgs, Candidate.Sig.NumPositionals);
                var TypeMismatch = false;
                for (int i = 0; i < TypeCheckCount; i++) {
                    var Arg = NativeCapture.Positionals[i];
                    var Type = Candidate.Sig.Parameters[i].Type;
                    if (Type != null && Ops.unbox_int(TC, Type.STable.TypeCheck(TC, Arg.STable.WHAT, Type)) == 0)
                    {
                        TypeMismatch = true;
                        break;
                    }
                    var Definedness = Candidate.Sig.Parameters[i].Definedness;
                    if (Definedness != DefinednessConstraint.None)
                    {
                        var ArgDefined = Arg.STable.REPR.defined(null, Arg);
                        if (Definedness == DefinednessConstraint.DefinedOnly && !ArgDefined ||
                            Definedness == DefinednessConstraint.UndefinedOnly && ArgDefined)
                        {
                            TypeMismatch = true;
                            break;
                        }
                    }
                }
                if (TypeMismatch)
                    continue;

                /* If we get here, it's an admissable candidate; add to list. */
                PossiblesList.Add(Candidate);
            }

            // If we get here, no candidates matched.
            throw new Exception("No candidates found to dispatch to");
        }

        /// <summary>
        /// Sorts the candidates.
        /// </summary>
        /// <param name="Unsorted"></param>
        /// <returns></returns>
        private static RakudoCodeRef.Instance[] Sort(ThreadContext TC, RakudoObject[] Unsorted)
        {
            /* Allocate results array (just allocate it for worst case, which
             * is no ties ever, so a null between all of them, and then space
             * for the terminating null. */
            var NumCandidates = Unsorted.Length;
            var Result = new RakudoCodeRef.Instance[2 * NumCandidates + 1];

            /* Create a node for each candidate in the graph. */
            var Graph = new CandidateGraphNode[NumCandidates];
            for (int i = 0; i < NumCandidates; i++)
            {
                Graph[i] = new CandidateGraphNode()
                {
                    Candidate = (RakudoCodeRef.Instance)Unsorted[i],
                    Edges = new CandidateGraphNode[NumCandidates]
                };
            }

            /* Now analyze type narrowness of the candidates relative to each other
             * and create the edges. */
            for (int i = 0; i < NumCandidates; i++) {
                for (int j = 0; j < NumCandidates; j++) {
                    if (i == j)
                        continue;
                    if (IsNarrower(TC, Graph[i].Candidate, Graph[j].Candidate) != 0) {
                        Graph[i].Edges[Graph[i].EdgesOut] = Graph[j];
                        Graph[i].EdgesOut++;
                        Graph[j].EdgesIn++;
                    }
                }
            }

            /* Perform the topological sort. */
            int CandidatesToSort = NumCandidates;
            int ResultPos = 0;
            while (CandidatesToSort > 0)
            {
                int StartPoint = ResultPos;

                /* Find any nodes that have no incoming edges and add them to
                 * results. */
                for (int i = 0; i < NumCandidates; i++) {
                    if (Graph[i].EdgesIn == 0) {
                        /* Add to results. */
                        Result[ResultPos] = Graph[i].Candidate;
                        Graph[i].Candidate = null;
                        ResultPos++;
                        CandidatesToSort--;
                        Graph[i].EdgesIn = EDGE_REMOVAL_TODO;
                    }
                }
                if (StartPoint == ResultPos) {
                    throw new Exception("Circularity detected in multi sub types.");
                }

                /* Now we need to decrement edges in counts for things that had
                 * edges from candidates we added here. */
                for (int i = 0; i < NumCandidates; i++) {
                    if (Graph[i].EdgesIn == EDGE_REMOVAL_TODO) {
                        for (int j = 0; j < Graph[i].EdgesOut; j++)
                            Graph[i].Edges[j].EdgesIn--;
                        Graph[i].EdgesIn = EDGE_REMOVED;
                    }
                }

                /* This is end of a tied group, so leave a gap. */
                ResultPos++;
            }

            return Result;
        }

        /// <summary>
        /// Checks if one signature is narrower than another.
        /// </summary>
        /// <param name="a"></param>
        /// <param name="b"></param>
        /// <returns></returns>
        private static int IsNarrower(ThreadContext TC, RakudoCodeRef.Instance a, RakudoCodeRef.Instance b)
        {
            var Narrower = 0;
            var Tied = 0;
            int i, TypesToCheck;

            /* Work out how many parameters to compare, factoring in slurpiness
             * and optionals. */
            if (a.Sig.NumPositionals == b.Sig.NumPositionals)
                TypesToCheck = a.Sig.NumPositionals;
            else if (a.Sig.NumRequiredPositionals == b.Sig.NumRequiredPositionals)
                TypesToCheck = Math.Min(a.Sig.NumPositionals, b.Sig.NumPositionals);
            else
                return 0;

            /* Analyse each parameter in the two candidates. */
            for (i = 0; i < TypesToCheck; i++) {
                var TypeObjA = a.Sig.Parameters[i].Type;
                var TypeObjB = b.Sig.Parameters[i].Type;
                if (TypeObjA == TypeObjB)
                {
                    /* In a full Perl 6 multi dispatcher, you'd consider
                     * constraints here. */
                    Tied++;
                }
                else
                {
                    if (IsNarrowerType(TC, TypeObjA, TypeObjB))
                        Narrower++;
                    else if (!IsNarrowerType(TC, TypeObjB, TypeObjA))
                        Tied++;
                }
            }

            /* If one is narrower than the other from current analysis, we're done. */
            if (Narrower >= 1 && Narrower + Tied == TypesToCheck)
                return 1;

            /* If they aren't tied, we're also done. */
            else if (Tied != TypesToCheck)
                return 0;

            /* Otherwise, we see if one has a slurpy and the other not. A lack of
            * slurpiness makes the candidate narrower. Otherwise, they're tied. */
            return !a.Sig.HasSlurpyPositional() && b.Sig.HasSlurpyPositional() ? 1 : 0;
        }

        /// <summary>
        /// Compares two types to see if the first is narrower than the second.
        /// </summary>
        /// <returns></returns>
        public static bool IsNarrowerType(ThreadContext TC, RakudoObject A, RakudoObject B)
        {
            // If one of the types is null, then we know that's automatically
            // wider than anything.
            if (B == null && A != null)
                return true;
            else if (A == null || B == null)
                return false;

            // Otherwise, check with the type system.
            return Ops.unbox_int(TC, Ops.type_check(TC, A, B)) != 0;
        }
    }
}
