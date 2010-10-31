using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Possible constraints on whether a parameter must be defined or
    /// not (from the representation's point of view).
    /// </summary>
    public enum DefinednessConstraint
    {
        /// <summary>
        /// Don't mind (Type:_).
        /// </summary>
        None,

        /// <summary>
        /// Must be repr-defined (Type:D).
        /// </summary>
        DefinedOnly,

        /// <summary>
        /// Must be repr-undefined (Type:U).
        /// </summary>
        UndefinedOnly
    }
}
