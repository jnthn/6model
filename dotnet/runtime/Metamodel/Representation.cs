using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Metamodel
{
    /// <summary>
    /// All representations should implement this API.
    /// </summary>
    public abstract class Representation
    {
        /// <summary>
        /// Creates a new type object of this representation, and
        /// associates it with the given HOW. Also sets up a new
        /// representation instance if needed.
        /// </summary>
        /// <param name="HOW"></param>
        /// <returns></returns>
        public abstract RakudoObject type_object_for(RakudoObject HOW);

        /// <summary>
        /// Creates a new instance based on the type object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public abstract RakudoObject instance_of(RakudoObject WHAT);

        /// <summary>
        /// Checks if a given object is defined.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public abstract bool defined(RakudoObject Obj);

        /// <summary>
        /// Gets the current value for an attribute.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public abstract RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name);

        /// <summary>
        /// Gets the current value for an attribute, obtained using the
        /// given hint.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <returns></returns>
        public abstract RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint);

        /// <summary>
        /// Binds the given value to the specified attribute.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <param name="Value"></param>
        public abstract void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value);

        /// <summary>
        /// Binds the given value to the specified attribute, using the
        /// given hint.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <param name="Value"></param>
        public abstract void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value);

        /// <summary>
        /// Gets the hint for the given attribute ID.
        /// </summary>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public abstract int hint_for(RakudoObject ClassHandle, string Name);

        /// <summary>
        /// Used with boxing. Sets an integer value, for representations that
        /// can hold one.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public abstract void set_int(RakudoObject Object, int Value);

        /// <summary>
        /// Used with boxing. Gets an integer value, for representations that
        /// can hold one.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public abstract int get_int(RakudoObject Object);

        /// <summary>
        /// Used with boxing. Sets a floating point value, for representations that
        /// can hold one.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public abstract void set_num(RakudoObject Object, double Value);

        /// <summary>
        /// Used with boxing. Gets a floating point value, for representations that
        /// can hold one.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public abstract double get_num(RakudoObject Object);

        /// <summary>
        /// Used with boxing. Sets a string value, for representations that
        /// can hold one.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public abstract void set_str(RakudoObject Object, string Value);

        /// <summary>
        /// Used with boxing. Gets a string value, for representations that
        /// can hold one.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Value"></param>
        public abstract string get_str(RakudoObject Object);
    }

    public static class Hints
    {
        /// <summary>
        /// Special value indicating that we have no hint.
        /// </summary>
        public const int NO_HINT = -1;
    }
}
