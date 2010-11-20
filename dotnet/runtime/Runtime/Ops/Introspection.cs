using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Ops relating to introspection.
    /// </summary>
    public static partial class Ops
    {
        /// <summary>
        /// Gets the sub object of the caller the specified number of levels
        /// down.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Level"></param>
        /// <returns></returns>
        public static RakudoObject get_caller_sub(ThreadContext TC, RakudoObject Level)
        {
            var ToLevel = Ops.unbox_int(TC, Level);
            var Context = TC.CurrentContext;
            while (ToLevel >= 0)
            {
                Context = Context.Caller;
                if (Context == null)
                    throw new Exception("Tried to get look too many levels down for a caller");
                ToLevel--;
            }
            return Context.StaticCodeObject;
        }

        /// <summary>
        /// Gets the sub object of the outer the specified number of levels
        /// down.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Level"></param>
        /// <returns></returns>
        public static RakudoObject get_outer_sub(ThreadContext TC, RakudoObject Level)
        {
            var ToLevel = Ops.unbox_int(TC, Level);
            var Context = TC.CurrentContext;
            while (ToLevel >= 0)
            {
                Context = Context.Outer;
                if (Context == null)
                    throw new Exception("Tried to get look too many levels down for an outer");
                ToLevel--;
            }
            return Context.StaticCodeObject;
        }
    }
}
