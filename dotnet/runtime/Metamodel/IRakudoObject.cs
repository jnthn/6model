using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Serialization;

namespace Rakudo.Metamodel
{
    /// <summary>
    /// The commonalities of every object.
    /// </summary>
    public interface IRakudoObject
    {
        /// <summary>
        /// Every object must have a way to refer to the shared table,
        /// which contains the commonalities this object has.
        /// </summary>
        SharedTable STable { get; set; }

        /// <summary>
        /// The serialization context this object belongs to.
        /// </summary>
        SerializationContext SC { get; set; }
    }
}
