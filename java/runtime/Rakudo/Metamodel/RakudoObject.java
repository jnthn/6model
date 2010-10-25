package Rakudo.Metamodel;

import Rakudo.Metamodel.SharedTable;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// The commonalities of every object.
/// </summary>
public abstract class RakudoObject
// public abstract class RakudoObject // the C# version
{
    /// <summary>
    /// Every object must have a way to refer to the shared table,
    /// which contains the commonalities this object has.
    /// </summary>
    private SharedTable sTable;
    public SharedTable getSTable() { return sTable; }
    public void setSTable( SharedTable st ) { sTable = st; }
    // SharedTable STable { get; set; } // the C# version

    /// <summary>
    /// The serialization context this object belongs to.
    /// </summary>
    private SerializationContext SC;
    public SerializationContext getSC() { return SC; }
    public void setSC( SerializationContext sc ) { SC = sc; }
    // SerializationContext SC { get; set; } // the C# version
}

