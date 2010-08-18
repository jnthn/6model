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
    public sealed class KnowHOWREPR : Representation
    {
        /// <summary>
        /// This represents an instance created with this underlying
        /// representation. We use .Net data types for out attribute
        /// and method store.
        /// </summary>
        internal class KnowHOWInstance : RakudoObject
        {
            public List<RakudoObject> Attributes;
            public Dictionary<string, RakudoObject> Methods;
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
        public override RakudoObject type_object_for(RakudoObject HOW)
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
        public override RakudoObject instance_of(RakudoObject WHAT)
        {
            var Object = new KnowHOWInstance(WHAT.STable);
            Object.Methods = new Dictionary<string, RakudoObject>();
            Object.Attributes = new List<RakudoObject>();
            return Object;
        }

        /// <summary>
        /// Checks if the object is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public override bool defined(RakudoObject Obj)
        {
            return ((KnowHOWInstance)Obj).Methods != null;
        }

        public override RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            throw new NotImplementedException();
        }

        public override RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            throw new NotImplementedException();
        }

        public override void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            throw new NotImplementedException();
        }

        public override void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
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
        public override int hint_for(RakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }
    }
}
