using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime.Exceptions
{
    /// <summary>
    /// This exception is thrown to actually unwind the (dotnet) stack after
    /// we run an exception handler.
    /// </summary>
    public class LeaveStackUnwinderException : Exception
    {
        /// <summary>
        /// The block we're looking for.
        /// </summary>
        public RakudoCodeRef.Instance TargetBlock;

        /// <summary>
        /// The value to exit with.
        /// </summary>
        public RakudoObject PayLoad;

        /// <summary>
        /// Creates a LeaveStackUnwinderException to 
        /// </summary>
        /// <param name="TargetBlock"></param>
        public LeaveStackUnwinderException(RakudoCodeRef.Instance TargetBlock, RakudoObject PayLoad)
        {
            this.TargetBlock = TargetBlock;
            this.PayLoad = PayLoad;
        }
    }
}
