using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Represents a parameter in a signature.
    /// </summary>
    public class Parameter
    {
        /// <summary>
        /// The type of the parameter.
        /// </summary>
        public IRakudoObject Type;

        /// <summary>
        /// The name of the lexical to bind the parameter to.
        /// </summary>
        public string VariableName;

        /// <summary>
        /// Name, for named parameters.
        /// </summary>
        public string Name;

        /// <summary>
        /// Parameter flags.
        /// </summary>
        public int Flags;

        /// <summary>
        /// Flag for optional parameters.
        /// </summary>
        public const int OPTIONAL_FLAG = 1;
        
        /// <summary>
        /// Flag for slurpy positional parameters.
        /// </summary>
        public const int POS_SLURPY_FLAG = 2;

        /// <summary>
        /// Flag for named slurpy parameters.
        /// </summary>
        public const int NAMED_SLURPY_FLAG = 4;
    }
}
