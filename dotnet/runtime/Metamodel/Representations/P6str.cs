using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation that we use (for now) for dealing with
    /// strings.
    /// </summary>
    public class P6str : IRepresentation, IBoxableRepresentation<string>
    {
        /// <summary>
        /// This is how the boxed form of a P6str looks.
        /// </summary>
        internal class Instance : IRakudoObject
        {
            public SharedTable STable { get; set; }
            public Serialization.SerializationContext SC { get; set; }
            public string Value;
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
            Object.Value = "";
            return Object;
        }

        /// <summary>
        /// Determines if the representation is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public bool defined(IRakudoObject Obj)
        {
            return ((Instance)Obj).Value != null;
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
        public string get_value(IRakudoObject Object)
        {
            return ((Instance)Object).Value;
        }

        /// <summary>
        /// Sets the native value inside this type (for boxing)
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public void set_value(IRakudoObject Object, string Value)
        {
            ((Instance)Object).Value = Value;
        }
    }
}
