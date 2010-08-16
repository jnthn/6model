using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Represents a signature.
    /// </summary>
    public class Signature
    {
        /// <summary>
        /// Creates a new Signature object instance.
        /// </summary>
        /// <param name="Parameters"></param>
        public Signature(Parameter[] Parameters)
        {
            this.Parameters = Parameters;
        }

        /// <summary>
        /// The parameters we have.
        /// </summary>
        public Parameter[] Parameters;
    }
}
