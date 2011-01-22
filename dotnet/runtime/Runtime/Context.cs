using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// A context represents a given invocation of a block. (Note this is
    /// fairly sketchy at the moment.)
    /// </summary>
    public class Context
    {
        /// <summary>
        /// The static chain.
        /// </summary>
        public Context Outer;

        /// <summary>
        /// The dynamic chain.
        /// </summary>
        public Context Caller;

        /// <summary>
        /// The static code object.
        /// </summary>
        public RakudoCodeRef.Instance StaticCodeObject;

        /// <summary>
        /// Lexpad.
        /// </summary>
        public Lexpad LexPad;

        /// <summary>
        /// The capture passed as part of the current call.
        /// </summary>
        public RakudoObject Capture;

        /// <summary>
        /// Creates an empty, uninitialized context.
        /// </summary>
        public Context()
        {
        }

        /// <summary>
        /// Initializes the context.
        /// </summary>
        /// <param name="StaticCodeObject"></param>
        /// <param name="Caller"></param>
        public Context(RakudoObject StaticCodeObject_Uncast, Context Caller, RakudoObject Capture)
        {
            // Set up static code object and caller pointers.
            var StaticCodeObject = (RakudoCodeRef.Instance)StaticCodeObject_Uncast;
            this.StaticCodeObject = StaticCodeObject;
            this.Caller = Caller;
            this.Capture = Capture;

            // Static sub object should have this as the current
            // context.
            StaticCodeObject.CurrentContext = this;

            // Lex pad should be an "instantiation" of the static one.
            // Instantiating a lexpad creates a new dynamic instance of it
            // from a static one, copying over the slot storage entries
            // from the static one but sharing the slot mapping.
            this.LexPad.SlotMapping = StaticCodeObject.StaticLexPad.SlotMapping;
            this.LexPad.Storage = (RakudoObject[])StaticCodeObject.StaticLexPad.Storage.Clone();

            // Set outer context.
            if (StaticCodeObject.OuterForNextInvocation != null)
            {
                this.Outer = StaticCodeObject.OuterForNextInvocation;
            }
            else if (StaticCodeObject.OuterBlock.CurrentContext != null)
            {
                this.Outer = StaticCodeObject.OuterBlock.CurrentContext;
            }
            else
            {
                // Auto-close. In this we go setting up fake contexts
                // that use the static lexpad until we find a real one.
                var CurContext = this;
                var OuterBlock = StaticCodeObject.OuterBlock;
                while (OuterBlock != null)
                {
                    // If we found a block with a context, we're done.
                    if (OuterBlock.CurrentContext != null)
                    {
                        CurContext.Outer = OuterBlock.CurrentContext;
                        break;
                    }

                    // Build the fake context.
                    var OuterContext = new Context();
                    OuterContext.StaticCodeObject = OuterBlock;
                    OuterContext.LexPad = OuterBlock.StaticLexPad;

                    // Link it.
                    CurContext.Outer = OuterContext;

                    // Step back one level.
                    CurContext = OuterContext;
                    OuterBlock = OuterBlock.OuterBlock;
                }
            }
        }
    }
}
