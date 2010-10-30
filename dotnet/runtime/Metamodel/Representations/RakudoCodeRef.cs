using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Runtime;
using Rakudo.Runtime.Exceptions;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// A representation for a low-level code object (something that actually
    /// references a piece of code that we'll run). This is used for things
    /// that serve the role of an only sub (that has a body) and a dispatcher
    /// (which has a body as well as a list of candidates that it operates
    /// on).
    /// </summary>
    public sealed class RakudoCodeRef : Representation
    {
        /// <summary>
        /// Instance that uses the RakudoCodeRef representation.
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
            public Lexpad StaticLexPad;
            
            /// <summary>
            /// Our static outer block.
            /// </summary>
            public Instance OuterBlock;

            /// <summary>
            /// Signature object.
            /// </summary>
            public Signature Sig;

            /// <summary>
            /// Exception handlers this block has, if any.
            /// </summary>
            public Handler[] Handlers;

            /// <summary>
            /// If this is a dispatcher, this is the list of dispatchees that
            /// it will operate over.
            /// </summary>
            public RakudoObject[] Dispatchees;

            /// <summary>
            /// The context currently using this sub.
            /// </summary>
            public Context CurrentContext;

            /// <summary>
            /// The outer context to use for the next invocation, if any.
            /// </summary>
            public Context OuterForNextInvocation;
            
            /// <summary>
            /// Creates a new instance with the given S-Table.
            /// </summary>
            /// <param name="STable"></param>
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
        public override RakudoObject type_object_for(ThreadContext TC, RakudoObject MetaPackage)
        {
            // Do the usual bits of setup for the type-object.
            var STable = new SharedTable();
            STable.HOW = MetaPackage;
            STable.REPR = this;
            STable.WHAT = new Instance(STable);

            // Also twiddle the S-Table's Invoke to invoke the contained
            // function.
            STable.Invoke = (TCi, Obj, Cap) =>
                ((RakudoCodeRef.Instance)Obj).Body(TCi, Obj, Cap);

            return STable.WHAT;
        }

        /// <summary>
        /// Creates an instance of the type with the given type object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public override RakudoObject instance_of(ThreadContext TC, RakudoObject WHAT)
        {
            var Object = new Instance(WHAT.STable);
            return Object;
        }

        /// <summary>
        /// Determines if the representation is defined or not.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public override bool defined(ThreadContext TC, RakudoObject Obj)
        {
            return ((Instance)Obj).Body != null;
        }

        public override RakudoObject get_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public override RakudoObject get_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public override void bind_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

        public override void bind_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
        {
            throw new InvalidOperationException("RakudoCodeRef objects cannot store additional attributes.");
        }

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
