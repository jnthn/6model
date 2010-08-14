using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation that uses a hash of hash representation to store
    /// the attributes. No real restriction on the sort of class it can be
    /// used with. Also dead easy to implement. :-)
    /// </summary>
    public sealed class P6hash : IRepresentation
    {
        /// <summary>
        /// This class represents our instances. It's inner workings are
        /// entirely private to this representation, which is the only
        /// thing that knows how it looks on the inside.
        /// XXX Once we do this production level, we really need to
        /// consider concurrency in accesses to the Dictionary. But
        /// this is OK for a prototype. -- jnthn
        /// </summary>
        private sealed class Instance : IRakudoObject
        {
            public SharedTable STable { get; set; }
            public Serialization.SerializationContext SC { get; set; }
            public Dictionary<IRakudoObject, Dictionary<string, IRakudoObject>> Storage;
            public Instance(SharedTable STable)
            {
                this.STable = STable;
            }
        }

        /// <summary>
        /// Creates a type object that references the given HOW and
        /// this REPR; note we just use the singleton instance for
        /// all of them, since the REPR stores nothing distinct.
        /// </summary>
        /// <param name="HOW"></param>
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
        /// Allocates and returns a new object based upon the type object
        /// supplied.
        /// </summary>
        /// <param name="HOW"></param>
        /// <returns></returns>
        public IRakudoObject instance_of(IRakudoObject WHAT)
        {
            var Object = new Instance(WHAT.STable);
            Object.Storage = new Dictionary<IRakudoObject, Dictionary<string, IRakudoObject>>();
            return Object;
        }

        /// <summary>
        /// Checks if the object is defined, which boils down to "is
        /// this a type object", which in trun means "did we allocate
        /// any storage".
        /// </summary>
        /// <param name="Object"></param>
        /// <returns></returns>
        public bool defined(IRakudoObject Object)
        {
            return ((Instance)Object).Storage != null;
        }

        /// <summary>
        /// Gets the attribute with the given value.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public IRakudoObject get_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name)
        {
            // If no storage ever allocated, trivially no value. Otherwise,
            // return what we find.
            var I = (Instance)Object;
            if (I.Storage == null || !I.Storage.ContainsKey(ClassHandle))
                return null;
            var ClassStore = I.Storage[ClassHandle];
            return ClassStore.ContainsKey(Name) ? ClassStore[Name] : null;
        }

        /// <summary>
        /// This representation doesn't use hints, so this just delegates
        /// straight off to the hint-less version.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <returns></returns>
        public IRakudoObject get_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint)
        {
            return get_attribute(Object, ClassHandle, Name);
        }

        /// <summary>
        /// Binds an attribute to the given value.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <param name="Value"></param>
        public void bind_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name, IRakudoObject Value)
        {
            // If no storage at all, allocate some.
            var I = (Instance)Object;
            if (I.Storage == null)
                I.Storage = new Dictionary<IRakudoObject, Dictionary<string, IRakudoObject>>();
            if (!I.Storage.ContainsKey(ClassHandle))
                I.Storage.Add(ClassHandle, new Dictionary<string, IRakudoObject>());
            
            // Now stick in the name slot for the class storage, creating if it
            // needed.
            var ClassStore = I.Storage[ClassHandle];
            if (ClassStore.ContainsKey(Name))
                ClassStore[Name] = Value;
            else
                ClassStore.Add(Name, Value);
        }

        /// <summary>
        /// This representation doesn't do hints, so this delegates straight
        /// off to the hint-less version.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <param name="Value"></param>
        public void bind_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint, IRakudoObject Value)
        {
            bind_attribute(Object, ClassHandle, Name, Value);
        }

        /// <summary>
        /// No hints for P6Hash.
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
