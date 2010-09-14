package Rakudo.Metamodel.Representations;

import java.util.HashMap;     // HashMap
import Rakudo.Runtime.Context;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Runtime.Lexpad;
import Rakudo.Runtime.Parameter;
import Rakudo.Runtime.Signature;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// A representation for low-level code references. This is something
/// specific to this Rakudo backend, not something standard accross all
/// Rakudo backends.
/// </summary>
public final class RakudoCodeRef implements Representation
{
    // Interface for the purpose of constructing anonymous classes that
    // implement it, as the Java equivalent of a C# lambda expression.
    // Used in for example: CodeObjectUtility
    public interface IFunc_Body {
        public RakudoObject Invoke(ThreadContext tc, RakudoObject ro1, RakudoObject ro2);
    }

    /// <summary>
    /// This is how the boxed form of a P6str looks.
    /// </summary>
    public final class Instance implements RakudoObject
    {
        private SharedTable st;
        public SharedTable getSTable(){return st;}
        public void setSTable( SharedTable st ){return;}

        private SerializationContext sc;
        public SerializationContext getSC() { return sc; }
        public void setSC( SerializationContext sc ) {;}

        /// <summary>
        /// The code body - the thing that actually runs instructions.
        /// </summary>
        public RakudoCodeRef.IFunc_Body Body; // IFunc_Body is defined above Instance
        // public Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject> Body;

        /// <summary>
        /// The static lexpad.
        /// </summary>
        public Lexpad StaticLexPad;
        
        /// <summary>
        /// Our static outer block.
        /// </summary>
        public Instance OuterBlock;

        /// <summary>
        /// Signature object.
        /// </summary>
        public Signature Sig;

        /// <summary>
        /// The context currently using this sub.
        /// </summary>
        public Context CurrentContext;

        /// <summary>
        /// Constructor.
        /// </summary>
        public Instance(SharedTable sharedTable)
        {
            this.setSTable(sharedTable);
            // this.STable = sharedTable; // the C# version is a property
        }
    }

    /// <summary>
    /// Create a new type object.
    /// </summary>
    /// <param name="MetaPackage"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(ThreadContext tc, RakudoObject MetaPackage)
    {
        // Do the usual bits of setup for the type-object.
        SharedTable sTable = new SharedTable();
        sTable.HOW = MetaPackage;
        sTable.REPR = this;
        sTable.WHAT = new Instance(sTable);

        // Also twiddle the S-Table's Invoke to invoke the contained
        // function.
        sTable.Invoke = new IFunc_Body() { // create an anonymous class
            public RakudoObject Invoke( ThreadContext TC, RakudoObject Obj, RakudoObject Cap )
            {
                // TODO ((RakudoCodeRef.Instance)Obj).Body(TC, Obj, Cap);
                return null; // TODO remove
            }
        };
        // the C# version:
        // STable.Invoke = (TC, Obj, Cap) =>
        //     ((RakudoCodeRef.Instance)Obj).Body(TC, Obj, Cap);

        return sTable.WHAT;
    }

    /// <summary>
    /// Creates an instance of the type with the given type object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public RakudoObject instance_of(ThreadContext tc, RakudoObject rakudoObject)
    {
        return new Instance(rakudoObject.getSTable());
        // return new Instance(rakudoObject.STable()); // the C# version is a property
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject rakudoObject)
    {
        return false;
// TODO return ((Instance)rakudoObject).Body != null;
    }

    public RakudoObject get_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public RakudoObject get_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public void bind_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public void bind_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public int hint_for(ThreadContext tc, RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }

    public void set_int(ThreadContext tc, RakudoObject Object, int Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public int get_int(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
    }

    public void set_num(ThreadContext tc, RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public double get_num(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public void set_str(ThreadContext tc, RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native string");
    }

    public String get_str(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native string");
    }
}

