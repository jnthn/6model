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
    public abstract class RakudoObject
    {
        /// <summary>
        /// Every object must have a way to refer to the shared table,
        /// which contains the commonalities this object has.
        /// </summary>
        public SharedTable STable;

        /// <summary>
        /// The serialization context this object belongs to.
        /// </summary>
        public SerializationContext SC;
    }
}
