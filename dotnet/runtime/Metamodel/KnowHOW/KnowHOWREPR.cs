using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel.KnowHOW
{
    /// <summary>
    /// We have a REPR especially for the KnowHOW, which is part of the
    /// "bootstrap".
    /// </summary>
    public class KnowHOWREPR : IRepresentation
    {
        /// <summary>
        /// This represents an instance created with this underlying
        /// representation. We use .Net data types for out attribute
        /// and method store.
        /// </summary>
        internal class KnowHOWInstance : IRakudoObject
        {
            public SharedTable STable { get; set; }
            public Serialization.SerializationContext SC { get; set; }
            public List<IRakudoObject> Attributes;
            public Dictionary<string, IRakudoObject> Methods;
            public KnowHOWInstance(SharedTable STable)
            {
                this.STable = STable;
            }
        }

        /// <summary>
        /// Gets a type object pointing to the given HOW.
        /// </summary>
        /// <param name="HOW"></param>
        /// <returns></returns>
        public IRakudoObject type_object_for(IRakudoObject HOW)
        {
            var STable = new SharedTable();
            STable.HOW = HOW;
            STable.REPR = this;
            STable.WHAT = new KnowHOWInstance(STable);
            return STable.WHAT;
        }

        /// <summary>
        /// Create an instance of the given object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public IRakudoObject instance_of(IRakudoObject WHAT)
        {
            var Object = new KnowHOWInstance(WHAT.STable);
            Object.Methods = new Dictionary<string, IRakudoObject>();
            Object.Attributes = new List<IRakudoObject>();
            return Object;
        }

        /// <summary>
        /// Checks if the object is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public bool defined(IRakudoObject Obj)
        {
            return ((KnowHOWInstance)Obj).Methods != null;
        }

        public IRakudoObject get_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name)
        {
            throw new NotImplementedException();
        }

        public IRakudoObject get_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint)
        {
            throw new NotImplementedException();
        }

        public void bind_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name, IRakudoObject Value)
        {
            throw new NotImplementedException();
        }

        public void bind_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint, IRakudoObject Value)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// We have attribute access hints for within the KnowHOW REPR, which
        /// we just manually map to the indexes.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public int hint_for(IRakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }
    }
}
