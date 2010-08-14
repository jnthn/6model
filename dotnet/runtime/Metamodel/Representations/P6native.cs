using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation that we use (for now) for dealing with
    /// native types in their boxed forms.
    /// </summary>
    public class P6native<TValue> : IRepresentation, IBoxableRepresentation<TValue> where TValue : struct
    {
        /// <summary>
        /// This is how the boxed form of a P6native looks like.
        /// </summary>
        internal class Instance : IRakudoObject
        {
            public SharedTable STable { get; set; }
            public Serialization.SerializationContext SC { get; set; }
            public Nullable<TValue> Value;
            public Instance(SharedTable STable)
            {
                this.STable = STable;
            }
        }

        /// <summary>
        /// Create a new type object.
        /// </summary>
        /// <param name="MetaPackage"></param>
        /// <returns></returns>
        public IRakudoObject type_object_for(IRakudoObject MetaPackage)
        {
            var STable = new SharedTable();
            STable.HOW = MetaPackage;
            STable.REPR = this;
            STable.WHAT = new Instance(STable);
            return STable.WHAT;
        }

        /// <summary>
        /// Creates an instance of the type with the given type object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public IRakudoObject instance_of(IRakudoObject WHAT)
        {
            var Object = new Instance(WHAT.STable);
            Object.Value = default(TValue);
            return Object;
        }

        /// <summary>
        /// Determines if the representation is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public bool defined(IRakudoObject Obj)
        {
            return ((Instance)Obj).Value.HasValue;
        }

        public IRakudoObject get_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public IRakudoObject get_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public void bind_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name, IRakudoObject Value)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public void bind_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint, IRakudoObject Value)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public int hint_for(IRakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }

        /// <summary>
        /// Gets the native value inside this type (for unboxing).
        /// </summary>
        /// <param name="Object"></param>
        /// <returns></returns>
        public TValue get_value(IRakudoObject Object)
        {
            return ((Instance)Object).Value.Value;
        }

        /// <summary>
        /// Sets the native value inside this type (for boxing)
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public void set_value(IRakudoObject Object, TValue Value)
        {
            ((Instance)Object).Value = Value;
        }
    }
}
