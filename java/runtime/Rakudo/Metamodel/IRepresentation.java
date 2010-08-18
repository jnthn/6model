package Rakudo.Metamodel;

import Rakudo.Metamodel.IRakudoObject;

/// <summary>
/// All representations should implement this API.
/// </summary>
public interface IRepresentation
{
    /// <summary>
    /// Creates a new type object of this representation, and
    /// associates it with the given HOW. Also sets up a new
    /// representation instance if needed.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    IRakudoObject type_object_for(IRakudoObject HOW);

    /// <summary>
    /// Creates a new instance based on the type object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    IRakudoObject instance_of(IRakudoObject WHAT);

    /// <summary>
    /// Checks if a given object is defined.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    boolean defined(IRakudoObject Obj);

    /// <summary>
    /// Gets the current value for an attribute.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    IRakudoObject get_attribute(IRakudoObject Object, IRakudoObject ClassHandle, String Name);

    /// <summary>
    /// Gets the current value for an attribute, obtained using the
    /// given hint.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    IRakudoObject get_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, String Name, int Hint);

    /// <summary>
    /// Binds the given value to the specified attribute.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Value"></param>
    void bind_attribute(IRakudoObject Object, IRakudoObject ClassHandle, String Name, IRakudoObject Value);

    /// <summary>
    /// Binds the given value to the specified attribute, using the
    /// given hint.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <param name="Value"></param>
    void bind_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, String Name, int Hint, IRakudoObject Value);

    /// <summary>
    /// Gets the hint for the given attribute ID.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    int hint_for(IRakudoObject ClassHandle, String Name);
}
