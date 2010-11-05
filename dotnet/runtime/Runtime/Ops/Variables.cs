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
        /// Gets a lexical variable of the given name.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public static RakudoObject get_lex(ThreadContext TC, string Name)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                    return CurContext.LexPad.Storage[Index];
                CurContext = CurContext.Outer;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the lexical scope");
        }

        /// <summary>
        /// Gets a lexical variable of the given name, but skips the current
        /// scope.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public static RakudoObject get_lex_skip_current(ThreadContext TC, string Name)
        {
            var CurContext = TC.CurrentContext.Outer;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                    return CurContext.LexPad.Storage[Index];
                CurContext = CurContext.Outer;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the lexical scope");
        }

        /// <summary>
        /// Binds the given value to a lexical variable of the given name.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public static RakudoObject bind_lex(ThreadContext TC, string Name, RakudoObject Value)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                {
                    CurContext.LexPad.Storage[Index] = Value;
                    return Value;
                }
                CurContext = CurContext.Outer;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the lexical scope");
        }

        /// <summary>
        /// Looks up a variable in the dynamic scope.
        /// </summary>
        /// <param name="C"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject get_dynamic(ThreadContext TC, string Name)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                    return CurContext.LexPad.Storage[Index];
                CurContext = CurContext.Caller;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the dynamic scope");
        }

        /// <summary>
        /// Binds the given value to a variable in the dynamic scope.
        /// </summary>
        /// <param name="C"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject bind_dynamic(ThreadContext TC, string Name, RakudoObject Value)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                {
                    CurContext.LexPad.Storage[Index] = Value;
                    return Value;
                }
                CurContext = CurContext.Caller;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the dynamic scope");
        }
    }
}
