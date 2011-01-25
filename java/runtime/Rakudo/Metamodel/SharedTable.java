package Rakudo.Metamodel;
import java.util.HashMap;
import Rakudo.Metamodel.ISpecialFindMethod;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Runtime.CaptureHelper;
import Rakudo.Runtime.Ops;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Serialization.SerializationContext;
/// <summary>
/// This represents the commonalities shared by many instances of
/// a given "type". Type in this context refers to a given combination
/// of meta-object and representation.
/// </summary>
public final class SharedTable  // C# has public sealed
{
    /// <summary>
    /// The representation object that manages object layout.
    /// </summary>
    public Representation REPR;

    /// <summary>
    /// The HOW (which is the meta-package). If we do $obj.HOW then
    /// it will refer to a getting of this field.
    /// </summary>
    public RakudoObject HOW;

    /// <summary>
    /// The type-object. If we do $obj.WHAT then it will refer to a 
    /// getting of this field.
    /// </summary>
    public RakudoObject WHAT;

    /// <summary>
    /// We keep a cache of the find_method method.
    /// </summary>
    private RakudoObject CachedFindMethod; // C# has internal

    /// <summary>
    /// Cache of methods by name. Published by meta-objects that choose
    /// to do so.
    /// </summary>
    private HashMap<String, RakudoObject> MethodCache; // C# has internal

    /// <summary>
    /// Sometimes, we may want to install a hook for overriding method
    /// finding. This does that. (We used to just give this a default
    /// closure, but it makes dispatch a bit more expensive, and this is
    /// path is red hot...)
    /// </summary>
    public ISpecialFindMethod SpecialFindMethod; // C# has public Func<ThreadContext, RakudoObject, string, int, RakudoObject>

    /// <summary>
    /// This finds a method with the given name or using a hint.
    /// </summary>
    public RakudoObject FindMethod(ThreadContext tc, RakudoObject obj, String name, int hint)
    {
        RakudoObject cachedMethod;

        // Does this s-table have a special overridden finder?
        if (SpecialFindMethod != null)
            return SpecialFindMethod.SpecialFindMethod(tc, obj, name, hint);

        // See if we can find it by hint.
        if (hint != Hints.NO_HINT && obj.getSTable().VTable != null && hint < obj.getSTable().VTable.length)
        {
            // Yes, just grab it from the v-table.
            return obj.getSTable().VTable[hint];
        }

        // Otherwise, try method cache.
        else if (MethodCache != null && MethodCache.containsKey(name))
            { cachedMethod = MethodCache.get(name); return cachedMethod; }

        // Otherwise, go ask the meta-object.
        else
        {
            // Find the find_method method.
            RakudoObject HOW = obj.getSTable().HOW;
            RakudoObject method = obj.getSTable().CachedFindMethod;
            if (method == null)
                obj.getSTable().CachedFindMethod = method = HOW.getSTable().FindMethod(
                    tc, HOW, "find_method", Hints.NO_HINT);

            // Call it.
            RakudoObject capture = CaptureHelper.FormWith(new RakudoObject[] { HOW, obj, Ops.box_str(tc, name, tc.DefaultStrBoxType) });
            return method.getSTable().Invoke(tc, method, capture);
        }
    }

    /// <summary>
    /// We keep a cache of the postcircumfix:<( )> method.
    /// </summary>
    private RakudoObject CachedInvoke; // C# has internal

    /// <summary>
    /// Hook for overriding invocation.
    /// </summary>
    public RakudoCodeRef.IFunc_Body SpecialInvoke; // C# has Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject>

        /// <summary>
        /// Invokes. By default, looks up a postcircumfix:<( )> and runs that.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Obj"></param>
        /// <param name="Cap"></param>
        /// <returns></returns>
    public RakudoObject Invoke(ThreadContext threadContext, RakudoObject method, RakudoObject capture)
    {
        if (SpecialInvoke != null)
            return SpecialInvoke.Invoke(threadContext, method, capture);
        SharedTable sharedTable = method.getSTable();
        RakudoObject invokable = sharedTable.CachedInvoke; if (invokable == null) { invokable = sharedTable.CachedInvoke = method.getSTable().FindMethod(threadContext, method, "postcircumfix:<( )>", Hints.NO_HINT); }
        return invokable.getSTable().Invoke(threadContext, method, capture);
    }

    /// <summary>
    /// For types where we know all the possible types it could match up
    /// front, this type check cache can be published.
    /// </summary>
    public RakudoObject[] TypeCheckCache;

    /// <summary>
    /// Used to do a type check. If one was published, looks in the type
    /// check cache. If there is none, calls .HOW.type_check.
    /// </summary>
    /// <param name="TC"></param>
    /// <param name="CheckAgainst"></param>
    /// <param name="Checkee"></param>
    /// <returns></returns>
    public RakudoObject TypeCheck(ThreadContext threadContext, RakudoObject object, RakudoObject checkee)
    {
        if (TypeCheckCache != null)
        {
            for (int i = 0; i < TypeCheckCache.length; i++)
                if (TypeCheckCache[i] == checkee)
                    return Ops.box_int(threadContext, 1, threadContext.DefaultBoolBoxType);
            return Ops.box_int(threadContext, 0, threadContext.DefaultBoolBoxType);
        }
        else
        {
            RakudoObject checker = HOW.getSTable().FindMethod(threadContext, HOW, "type_check", Hints.NO_HINT);
            RakudoObject capture = CaptureHelper.FormWith(new RakudoObject[] { HOW, object, checkee });
            return checker.getSTable().Invoke(threadContext, checker, capture);
        }
    }

    /// <summary>
    /// The serialization context of this STable, if any.
    /// </summary>
    public SerializationContext SC;

    /// <summary>
    /// The generated v-table, if any.
    /// </summary>
    public RakudoObject[] VTable;

    /// <summary>
    /// The unique ID for this type. Note that this ID is not ever,
    /// ever, ever, ever to be used as a handle for the type for looking
    /// it up. It is only ever valid to use in a cache situation where a
    /// reference to the STable is held for at least as long as the cache
    /// will exist. It is also NOT going to be the same between runs (or
    /// at lesat not automatically), and will be set up whenever the STable
    /// is deserialized. Thus never, ever serialize this ID anywhere; it's
    /// for strictly for per-run scoped caches _only_. You have been warned.
    /// </summary>
    public long getTypeCacheID () {
        long id;
        synchronized(this) {   // is locking the entire SharedTable too coarse?
            TypeCacheIDSource += 4;
            id = TypeCacheIDSource;
        } // TODO: replace synchronized with java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock

        return id;
    }

    /// <summary>
    /// Source of type IDs. The lowest one is 4. This is to make the lower
    /// two bits available for defined/undefined/don't care flags for the
    /// multi dispatch cache, which is the primary user of these IDs.
    /// </summary>
    private static long TypeCacheIDSource = 4;

}

