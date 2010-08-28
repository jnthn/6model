package Rakudo.Metamodel.Representations;

import java.util.HashMap;     // HashMap
import Rakudo.Runtime.Context;
//import Rakudo.Runtime.ThreadContext;
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
/* TODO
        /// <summary>
        /// The code body - the thing that actually runs instructions.
        /// </summary>
//      public Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject> Body;
*/

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
/*        
        /// <summary>
        /// Constructor.
        /// </summary>
        public Instance(SharedTable sharedTable)
        {
            this.STable = sharedTable;
        }
*/
    }
    public RakudoObject type_object_for(RakudoObject how){return new Instance();}
    public RakudoObject instance_of(RakudoObject what){return new Instance();}
    public boolean defined(RakudoObject obj){return false;}
/* TODO
    /// <summary>
    /// Create a new type object.
    /// </summary>
    /// <param name="MetaPackage"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(RakudoObject MetaPackage)
    {
        // Do the usual bits of setup for the type-object.
        SharedTable STable = new SharedTable();
        STable.HOW = MetaPackage;
        STable.REPR = this;
//      STable.WHAT = new Instance(STable);

        // Also twiddle the S-Table's Invoke to invoke the contained
        // function.
        STable.Invoke = new IRakudoObject_Invokable() {
//          public RakudoObject Invoke( TC, Obj, Cap ) {
//              ((RakudoCodeRef.Instance)Obj).Body(TC, Obj, Cap);
//          }
        };
//      STable.Invoke = (TC, Obj, Cap) =>
//          ((RakudoCodeRef.Instance)Obj).Body(TC, Obj, Cap);

        return STable.WHAT;
    }

    /// <summary>
    /// Creates an instance of the type with the given type object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns> TODO
    public RakudoObject instance_of(RakudoObject rakudoobject)
    {
        return new Instance(rakudoobject.STable);
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(RakudoObject rakudoobject)
    {
        return ((Instance)rakudoobject).Body != null;
    }
*/

    public RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public int hint_for(RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }

    public void set_int(RakudoObject Object, int Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public int get_int(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
    }

    public void set_num(RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public double get_num(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public void set_str(RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native string");
    }

    public String get_str(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native string");
    }
}

