using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation that we use (for now) for native captures.
    /// </summary>
    public sealed class P6capture : Representation
    {
        /// <summary>
        /// This is how a Capture looks.
        /// </summary>
        internal sealed class Instance : RakudoObject
        {
            public RakudoObject[] Positionals;
            public Dictionary<string, RakudoObject> Nameds;
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
            return Object;
        }

        /// <summary>
        /// Determines if the representation is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public override bool defined(RakudoObject O)
        {
            var Obj = (Instance)O;
            return Obj.Positionals != null || Obj.Nameds != null;
        }

        public override RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public override RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public override void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public override void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public override int hint_for(RakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }
    }
}
