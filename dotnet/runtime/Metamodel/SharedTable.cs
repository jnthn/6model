using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Runtime;

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
        /// This finds a method with the given name or using a hint.
        /// </summary>
        public Func<ThreadContext, IRakudoObject, string, int, IRakudoObject> FindMethod =
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
                    var Meth = HOW.STable.FindMethod(TC, HOW, "find_method", Hints.NO_HINT);
                    
                    // Call it.
                    var Cap = CaptureHelper.FormWith(HOW, Ops.box<string>(Name, TC.Domain.DefaultStrBoxType));
                    return Meth.STable.Invoke(TC, Meth, Cap);
                }
            };

        /// <summary>
        /// The default invoke looks up a postcircumfix:<( )> and runs that.
        /// XXX Cache the hint where we can.
        /// </summary>
        public Func<ThreadContext, IRakudoObject, IRakudoObject, IRakudoObject> Invoke =
            (TC, Obj, Cap) =>
            {
                var Invokable = Obj.STable.FindMethod(TC, Obj, "postcircumfix:<( )>", Hints.NO_HINT);
                return Invokable.STable.Invoke(TC, Obj, Cap);
            };

        /// <summary>
        /// The representation object that manages object layout.
        /// </summary>
        public IRepresentation REPR;

        /// <summary>
        /// The HOW (which is the meta-package). If we do $obj.HOW then
        /// it will refer to a getting of this field.
        /// </summary>
        public IRakudoObject HOW;

        /// <summary>
        /// The type-object. If we do $obj.WHAT then it will refer to a 
        /// getting of this field.
        /// </summary>
        public IRakudoObject WHAT;

        /// <summary>
        /// The generated v-table, if any.
        /// </summary>
        public IRakudoObject[] VTable;
    }
}
