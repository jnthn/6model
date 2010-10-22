using System;
using Rakudo.Metamodel;

namespace Rakudo.Runtime.Exceptions
{
    /// <summary>
    /// Represents an exception handler.
    /// </summary>
    public struct Handler
    {
        /// <summary>
        /// The type of exception that the handler accepts.
        /// </summary>
        public int Type;

        /// <summary>
        /// Something invokable that will handle the exception.
        /// </summary>
        public RakudoObject HandleBlock;
    }
}
