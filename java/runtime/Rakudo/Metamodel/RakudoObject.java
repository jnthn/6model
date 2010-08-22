package Rakudo.Metamodel;

import Rakudo.Serialization.SerializationContext;

/// <summary>
/// The commonalities of every object.
/// </summary>
public interface RakudoObject
{
    /// <summary>
    /// Every object must have a way to refer to the shared table,
    /// which contains the commonalities this object has.
    /// </summary>
    SharedTable getSTable();
    void setSTable( SharedTable st );
    // SharedTable STable { get; set; } // the C# version

    /// <summary>
    /// The serialization context this object belongs to.
    /// </summary>
    SerializationContext getSC();
    void setSC( SerializationContext sc );
    // SerializationContext SC { get; set; } // the C# version
}

