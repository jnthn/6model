using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Runtime;

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
            public RakudoObject Name;
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
        public override RakudoObject type_object_for(ThreadContext TC, RakudoObject HOW)
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
        public override RakudoObject instance_of(ThreadContext TC, RakudoObject WHAT)
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
        public override bool defined(ThreadContext TC, RakudoObject Obj)
        {
            return ((KnowHOWInstance)Obj).Methods != null;
        }

        public override RakudoObject get_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            throw new NotImplementedException();
        }

        public override RakudoObject get_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            throw new NotImplementedException();
        }

        public override void bind_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            throw new NotImplementedException();
        }

        public override void bind_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
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
        public override int hint_for(ThreadContext TC, RakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }

        public override void set_int(ThreadContext TC, RakudoObject Object, int Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native int");
        }

        public override int get_int(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native int");
        }

        public override void set_num(ThreadContext TC, RakudoObject Object, double Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native num");
        }

        public override double get_num(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native num");
        }

        public override void set_str(ThreadContext TC, RakudoObject Object, string Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native string");
        }

        public override string get_str(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native string");
        }
    }
}
