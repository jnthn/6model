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
        /// Creates a new parameter object instance.
        /// </summary>
        /// <param name="Type"></param>
        /// <param name="VariableName"></param>
        /// <param name="Name"></param>
        /// <param name="Flags"></param>
        public Parameter(RakudoObject Type, string VariableName, int VariableLexpadPosition,
            string Name, int Flags, DefinednessConstraint Definedness, RakudoObject DefaultValue)
        {
            this.Type = Type;
            this.VariableName = VariableName;
            this.VariableLexpadPosition = VariableLexpadPosition;
            this.Name = Name;
            this.DefaultValue = DefaultValue;
            this.Flags = Flags;
            this.Definedness = Definedness;
        }

        /// <summary>
        /// The type of the parameter.
        /// </summary>
        public RakudoObject Type;

        /// <summary>
        /// Whether a defined or undefined value is required.
        /// </summary>
        public DefinednessConstraint Definedness;

        /// <summary>
        /// The name of the lexical to bind the parameter to.
        /// </summary>
        public string VariableName;

        /// <summary>
        /// The position in the lexpad where the variable will be stored.
        /// </summary>
        public int VariableLexpadPosition;

        /// <summary>
        /// Name, for named parameters.
        /// </summary>
        public string Name;

        /// <summary>
        /// Default RakudoObject for optional parameters.
        /// </summary>
        public RakudoObject DefaultValue;

        /// <summary>
        /// Parameter flags.
        /// </summary>
        public int Flags;

        /// <summary>
        /// (Un-)flag for positional parameters.
        /// </summary>
        public const int POS_FLAG = 0;

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

        /// <summary>
        /// Flag for named parameters.
        /// </summary>
        public const int NAMED_FLAG = 8;

        public bool IsOptional()
        {
            return (Flags ^ OPTIONAL_FLAG) > 0;
        }
    }
}
