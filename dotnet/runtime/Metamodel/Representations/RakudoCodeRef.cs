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
    public sealed class RakudoCodeRef : Representation
    {
        /// <summary>
        /// This is how the boxed form of a P6str looks.
        /// </summary>
        public sealed class Instance : RakudoObject
        {
            /// <summary>
            /// The code body - the thing that actually runs instructions.
            /// </summary>
            public Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject> Body;
            
            /// <summary>
            /// The static lexpad.
            /// </summary>
            public Dictionary<string, RakudoObject> StaticLexPad;
            
            /// <summary>
            /// Our static outer block.
            /// </summary>
            public Instance OuterBlock;

            /// <summary>
            /// Signature object.
            /// </summary>
            public Signature Sig;

            /// <summary>
            /// The context currently using this sub.
            /// </summary>
            public Context CurrentContext;
            
            /// <summary>
            /// Things we have control over the dispatch of.
            /// </summary>
            public List<Instance> Dispatchees;
            
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
        public override bool defined(RakudoObject Obj)
        {
            return ((Instance)Obj).Body != null;
        }

        public override RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public override RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public override void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public override void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public override int hint_for(RakudoObject ClassHandle, string Name)
        {
            return Hints.NO_HINT;
        }

        public override void set_int(RakudoObject Object, int Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native int");
        }

        public override int get_int(RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native int");
        }

        public override void set_num(RakudoObject Object, double Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native num");
        }

        public override double get_num(RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native num");
        }

        public override void set_str(RakudoObject Object, string Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native string");
        }

        public override string get_str(RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native string");
        }
    }
}
