package Rakudo.Metamodel;
import Rakudo.Metamodel.IFindMethod;
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
public final class SharedTable  // public sealed in the C# version
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
    /// This finds a method with the given name or using a hint.
    /// </summary>
    public IFindMethod FindMethod = new IFindMethod() { // this anonymous class is a lambda in the C# version
        public RakudoObject FindMethod(ThreadContext tc, RakudoObject obj, String name, int hint)
        {
            // See if we can find it by hint.
            if (hint != Hints.NO_HINT && obj.getSTable().VTable != null && hint < obj.getSTable().VTable.length)
            {
                // Yes, just grab it from the v-table.
                return obj.getSTable().VTable[hint];
            }
            else
            {
                // Find the find_method method.
                RakudoObject HOW = obj.getSTable().HOW;
                RakudoObject meth = CachedFindMethod;
                if (meth == null)
                    obj.getSTable().CachedFindMethod = meth = HOW.getSTable().FindMethod.FindMethod(
                        tc, HOW, "find_method", Hints.NO_HINT);

                // Call it.
                RakudoObject capt = CaptureHelper.FormWith(new RakudoObject[] { HOW, Ops.box_str(tc, name, tc.DefaultStrBoxType) });
                return meth.getSTable().Invoke.Invoke(tc, meth, capt);
            }
        }
    };
    /// <summary>
    /// We keep a cache of the find_method method.
    /// </summary>
    private RakudoObject CachedFindMethod; // internal in the C# version

    /// <summary>
    /// The default invoke looks up a postcircumfix:<( )> and runs that.
    /// XXX Cache the hint where we can.
    /// </summary>
    public RakudoCodeRef.IFunc_Body Invoke = new RakudoCodeRef.IFunc_Body() { // this anonymous class is a lambda in the C# version
        public RakudoObject Invoke( ThreadContext tc, RakudoObject meth, RakudoObject capt )
        {
            SharedTable sTable = meth.getSTable();
            RakudoObject invokable = (sTable.CachedInvoke != null) ? sTable.CachedInvoke : (sTable.CachedInvoke = meth.getSTable().FindMethod.FindMethod(tc, meth, "postcircumfix:<( )>", Hints.NO_HINT));
            return invokable.getSTable().Invoke.Invoke(tc, meth, capt);
        }
    };
    /// <summary>
    /// We keep a cache of the postcircumfix:<( )> method.
    /// </summary>
    private RakudoObject CachedInvoke; // internal in the C# version

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
    public long TypeCacheID () {
        long id;
        synchronized(this) {   // is locking the entire SharedTable too coarse?
            TypeCacheIDSource += 4;
            id = TypeCacheIDSource;
        }
        return id;
    } // public long TypeCacheID = Interlocked.Increment(ref TypeIDSource); // the C# version

    /// <summary>
    /// Source of type IDs. The lowest one is 4. This is to make the lower
    /// two bits available for defined/undefined/don't care flags for the
    /// multi dispatch cache, which is the primary user of these IDs.
    /// </summary>
    private static long TypeCacheIDSource = 4;

}

