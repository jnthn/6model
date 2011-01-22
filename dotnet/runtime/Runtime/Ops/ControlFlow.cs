using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// This class implements the various vm::op options that are
    /// available.
    /// </summary>
    public static partial class Ops
    {
        /// <summary>
        /// If the first passed object reference is not null, returns it. Otherwise,
        /// returns the second passed object reference. (Note, we should one day drop
        /// this and implement it as a compiler transformation, to avoid having to
        /// look up the thing to vivify).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Check"></param>
        /// <param name="VivifyWith"></param>
        /// <returns></returns>
        public static RakudoObject vivify(ThreadContext TC, RakudoObject Check, RakudoObject VivifyWith)
        {
            return Check ?? VivifyWith;
        }

        /// <summary>
        /// Leaves the specified block, returning the specified value from it. This
        /// unwinds the stack.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Block"></param>
        /// <param name="ReturnValue"></param>
        /// <returns></returns>
        public static RakudoObject leave_block(ThreadContext TC, RakudoObject Block, RakudoObject ReturnValue)
        {
            throw new Exceptions.LeaveStackUnwinderException(Block as RakudoCodeRef.Instance, ReturnValue);
        }

        /// <summary>
        /// Throws the specified exception, looking for an exception handler in the
        /// dynamic scope.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="ExceptionObject"></param>
        /// <param name="ExceptionType"></param>
        /// <returns></returns>
        public static RakudoObject throw_dynamic(ThreadContext TC, RakudoObject ExceptionObject, RakudoObject ExceptionType)
        {
            int WantType = Ops.unbox_int(TC, ExceptionType);
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                if (CurContext.StaticCodeObject != null)
                {
                    var Handlers = CurContext.StaticCodeObject.Handlers;
                    if (Handlers != null)
                        for (int i = 0; i < Handlers.Length; i++)
                            if (Handlers[i].Type == WantType)
                                return Exceptions.ExceptionDispatcher.CallHandler(TC,
                                    Handlers[i].HandleBlock, ExceptionObject);
                }
                CurContext = CurContext.Caller;
            }
            Exceptions.ExceptionDispatcher.DieFromUnhandledException(TC, ExceptionObject);
            return null; // Unreachable; above call exits always.
        }

        /// <summary>
        /// Throws the specified exception, looking for an exception handler in the
        /// lexical scope.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="ExceptionObject"></param>
        /// <param name="ExceptionType"></param>
        /// <returns></returns>
        public static RakudoObject throw_lexical(ThreadContext TC, RakudoObject ExceptionObject, RakudoObject ExceptionType)
        {
            int WantType = Ops.unbox_int(TC, ExceptionType);
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                if (CurContext.StaticCodeObject != null)
                {
                    var Handlers = CurContext.StaticCodeObject.Handlers;
                    if (Handlers != null)
                        for (int i = 0; i < Handlers.Length; i++)
                            if (Handlers[i].Type == WantType)
                                return Exceptions.ExceptionDispatcher.CallHandler(TC,
                                    Handlers[i].HandleBlock, ExceptionObject);
                }
                CurContext = CurContext.Outer;
            }
            Exceptions.ExceptionDispatcher.DieFromUnhandledException(TC, ExceptionObject);
            return null; // Unreachable; above call exits always.
        }

        /// <summary>
        /// Makes the outer context of the provided block be set to the current
        /// context.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Block"></param>
        /// <returns></returns>
        public static RakudoObject capture_outer(ThreadContext TC, RakudoCodeRef.Instance Block)
        {
            Block.OuterForNextInvocation = TC.CurrentContext;
            return Block;
        }

        /// <summary>
        /// Creates a clone of the given code object, and makes its outer context
        /// be set to the current context.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Block"></param>
        /// <returns></returns>
        public static RakudoObject new_closure(ThreadContext TC, RakudoCodeRef.Instance Block)
        {
            // Clone all but OuterForNextInvocation and SC (since it's a new
            // object and doesn't live in any SC yet).
            var NewBlock = new RakudoCodeRef.Instance(Block.STable);
            NewBlock.Body = Block.Body;
            NewBlock.CurrentContext = Block.CurrentContext;
            NewBlock.Dispatchees = Block.Dispatchees;
            NewBlock.Handlers = Block.Handlers;            
            NewBlock.OuterBlock = Block.OuterBlock;
            NewBlock.Sig = Block.Sig;
            NewBlock.StaticLexPad = Block.StaticLexPad;

            // Set the outer for next invocation and return the cloned block.
            NewBlock.OuterForNextInvocation = TC.CurrentContext;
            return NewBlock;
        }
    }
}
