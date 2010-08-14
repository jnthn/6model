using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Runtime
{
    /// <summary>
    /// We have one of these per thread that we are running.
    /// </summary>
    public class ThreadContext
    {
        /// <summary>
        /// The execution domain that we're operating under.
        /// </summary>
        public ExecutionDomain Domain;

        /// <summary>
        /// The current context we're in.
        /// </summary>
        public Context CurrentContext;
    }
}
