using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Runtime;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation for low-level code references. This is something
    /// specific to this Rakudo backend, not something standard accross all
    /// Rakudo backends.
    /// </summary>
    public class RakudoCodeRef : IRepresentation
    {
        /// <summary>
        /// This is how the boxed form of a P6str looks.
        /// </summary>
        public class Instance : IRakudoObject
        {
            public SharedTable STable { get; set; }
            public Serialization.SerializationContext SC { get; set; }
            public Func<ThreadContext, IRakudoObject, IRakudoObject, IRakudoObject> Body;
            public Dictionary<string, IRakudoObject> StaticLexPad;
            public Instance OuterBlock;
            public Signature Sig;
            public Context CurrentContext;
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
            // Do the usual bits of setup for the type-object.
            var STable = new SharedTable();
            STable.HOW = MetaPackage;
            STable.REPR = this;
            STable.WHAT = new Instance(STable);

            // Also twiddle the S-Table's Invoke to invoke the contained
            // function.
            STable.Invoke = (TC, Obj, Cap) =>
                ((RakudoCodeRef.Instance)Obj).Body(TC, Obj, Cap);

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
        public bool defined(IRakudoObject Obj)
        {
            return ((Instance)Obj).Body != null;
        }

        public IRakudoObject get_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public IRakudoObject get_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public void bind_attribute(IRakudoObject Object, IRakudoObject ClassHandle, string Name, IRakudoObject Value)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public void bind_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, string Name, int Hint, IRakudoObject Value)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public int hint_for(IRakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }
    }
}
