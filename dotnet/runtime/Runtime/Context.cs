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
        /// Lexpad. Note that we'll in the end have something much smarter
        /// for this but it'll do for now.
        /// </summary>
        public Dictionary<string, IRakudoObject> LexPad;

        /// <summary>
        /// For internal use.
        /// XXX Kill this, probably.
        /// </summary>
        public Context()
        {
        }

        /// <summary>
        /// Initializes the context.
        /// </summary>
        /// <param name="StaticCodeObject"></param>
        /// <param name="Caller"></param>
        public Context(RakudoCodeRef.Instance StaticCodeObject, Context Caller)
        {
            // Set up static code object and caller pointers.
            this.StaticCodeObject = StaticCodeObject;
            this.Caller = Caller;

            // Static sub object should have this as the current
            // context.
            StaticCodeObject.CurrentContext = this;

            // Lex pad should be copy of the static one.
            // XXX This isn't quite what we want in the long run, but it
            // does fine for now.
            this.LexPad = new Dictionary<string, IRakudoObject>(StaticCodeObject.StaticLexPad);

            // Set outer context.
            var OuterBlock = StaticCodeObject.OuterBlock;
            if (OuterBlock.CurrentContext != null)
            {
                this.Outer = OuterBlock.CurrentContext;
            }
            else
            {
                // XXX Auto-close
                throw new NotImplementedException("Need to implement auto-close.");
            }
        }
    }
}
