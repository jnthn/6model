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
    public sealed class P6str : Representation, IBoxableRepresentation<string>
    {
        /// <summary>
        /// This is how the boxed form of a P6str looks.
        /// </summary>
        internal sealed class Instance : RakudoObject
        {
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
        public override RakudoObject type_object_for(RakudoObject MetaPackage)
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
        public override RakudoObject instance_of(RakudoObject WHAT)
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
        public override bool defined(RakudoObject Obj)
        {
            return ((Instance)Obj).Value != null;
        }

        public override RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
        {
            throw new InvalidOperationException("Boxed native types cannot store additional attributes.");
        }

        public override int hint_for(RakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }

        /// <summary>
        /// Gets the native value inside this type (for unboxing).
        /// </summary>
        /// <param name="Object"></param>
        /// <returns></returns>
        public string get_value(RakudoObject Object)
        {
            return ((Instance)Object).Value;
        }

        /// <summary>
        /// Sets the native value inside this type (for boxing)
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public void set_value(RakudoObject Object, string Value)
        {
            ((Instance)Object).Value = Value;
        }
    }
}
