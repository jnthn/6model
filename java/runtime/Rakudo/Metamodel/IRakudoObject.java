package Rakudo.Metamodel;

import Rakudo.Serialization.SerializationContext;

/// <summary>
/// The commonalities of every object.
/// </summary>
interface IRakudoObject
{

    /// <summary>
    /// Every object must have a way to refer to the shared table,
    /// which contains the commonalities this object has.
    /// </summary>
    // SharedTable STable { get; set; } // the C# version
    SharedTable getSTable();
    void setSTable( SharedTable st );

    /// <summary>
    /// The serialization context this object belongs to.
    /// </summary>
    //SerializationContext SC { get; set; } // the C# version
    SerializationContext getSC();
    void setSC( SerializationContext sc );
}

