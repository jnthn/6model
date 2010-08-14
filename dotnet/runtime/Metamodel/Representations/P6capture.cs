using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation that we use (for now) for native captures.
    /// </summary>
    public class P6capture : IRepresentation
    {
        /// <summary>
        /// This is how a Capture looks.
        /// </summary>
        internal class Instance : IRakudoObject
        {
            public SharedTable STable { get; set; }
            public Serialization.SerializationContext SC { get; set; }
            public IRakudoObject[] Positionals;
            public Dictionary<string, IRakudoObject> Nameds;
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
            return Object;
        }

        /// <summary>
        /// Determines if the representation is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public bool defined(IRakudoObject O)
        {
            var Obj = (Instance)O;
            return Obj.Positionals != null || Obj.Nameds != null;
        }

        public IRakudoObject get_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public IRakudoObject get_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public void bind_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name, IRakudoObject Value)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public void bind_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint, IRakudoObject Value)
        {
            throw new InvalidOperationException("Native captures cannot store additional attributes.");
        }

        public int hint_for(IRakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }
    }
}
