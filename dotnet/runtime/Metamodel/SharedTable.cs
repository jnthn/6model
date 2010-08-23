using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Rakudo.Runtime;
using Rakudo.Serialization;

namespace Rakudo.Metamodel
{
    /// <summary>
    /// This represents the commonalities shared by many instances of
    /// a given "type". Type in this context refers to a given combination
    /// of meta-object and representation.
    /// </summary>
    public sealed class SharedTable
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
        public Func<ThreadContext, RakudoObject, string, int, RakudoObject> FindMethod =
            (TC, Obj, Name, Hint) =>
            {
                // See if we can find it by hint.
                if (Hint != Hints.NO_HINT && Obj.STable.VTable != null && Hint < Obj.STable.VTable.Length)
                {
                    // Yes, just grab it from the v-table.
                    return Obj.STable.VTable[Hint];
                }
                else
                {
                    // Find the find_method method.
                    var HOW = Obj.STable.HOW;
                    RakudoObject Meth = Obj.STable.CachedFindMethod;
                    if (Meth == null)
                        Obj.STable.CachedFindMethod = Meth = HOW.STable.FindMethod(
                            TC, HOW, "find_method", Hints.NO_HINT);

                    // Call it.
                    var Cap = CaptureHelper.FormWith(new RakudoObject[] { HOW, Ops.box_str(TC, Name, TC.DefaultStrBoxType) });
                    return Meth.STable.Invoke(TC, Meth, Cap);
                }
            };

        /// <summary>
        /// We keep a cache of the find_method method.
        /// </summary>
        internal RakudoObject CachedFindMethod;

        /// <summary>
        /// The default invoke looks up a postcircumfix:<( )> and runs that.
        /// XXX Cache the hint where we can.
        /// </summary>
        public Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject> Invoke =
            (TC, Obj, Cap) =>
            {
                var STable = Obj.STable;
                var Invokable = STable.CachedInvoke ?? (STable.CachedInvoke = Obj.STable.FindMethod(TC, Obj, "postcircumfix:<( )>", Hints.NO_HINT));
                return Invokable.STable.Invoke(TC, Obj, Cap);
            };

        /// <summary>
        /// We keep a cache of the postcircumfix:<( )> method.
        /// </summary>
        internal RakudoObject CachedInvoke;

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
        public long TypeCacheID = Interlocked.Add(ref TypeCacheIDSource, 4);

        /// <summary>
        /// Source of type IDs. The lowest one is 4. This is to make the lower
        /// two bits available for defined/undefined/don't care flags for the
        /// multi dispatch cache, which is the primary user of these IDs.
        /// </summary>
        private static long TypeCacheIDSource = 4;
    }
}
