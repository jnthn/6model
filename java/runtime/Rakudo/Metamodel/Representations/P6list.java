package Rakudo.Metamodel.Representations;

import java.util.ArrayList;
//import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// This is a very first cut at a list representation. Essentially,
/// it just knows how to store a list of objects at the moment. At
/// some point we need to define the way that it will handle compact
/// arrays.
/// </summary>
public class P6list implements Representation
{
    public class Instance implements RakudoObject
    {
        // RakudoObject required implementation
        private SharedTable _SharedTable;
        private SerializationContext _SC;
        public SharedTable getSTable() {return _SharedTable;}
        public void setSTable( SharedTable st ){ _SharedTable = st;}
        public SerializationContext getSC(){return _SC;}
        public void setSC( SerializationContext sc ){ _SC = sc;}

        /// <summary>
        /// Just use a .Net List at the moment, but an array would
        /// be more efficient in the long run (though give us more
        /// stuff to implement ourselves).
        /// </summary>
        public ArrayList<RakudoObject> Storage;
        public Instance(SharedTable STable)
        {
            this.setSTable(STable);
        }
    }

    /// <summary>
    /// Create a new type object.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    public  RakudoObject type_object_for(ThreadContext TC, RakudoObject MetaPackage)
    {
        SharedTable STable = new SharedTable();
        STable.HOW = MetaPackage;
        STable.REPR = this;
        STable.WHAT = new Instance(STable);
        return STable.WHAT;
    }

    /// <summary>
    /// Creates an instance of the type with the given type object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public  RakudoObject instance_of(ThreadContext TC, RakudoObject WHAT)
    {
        Instance Object = new Instance(WHAT.getSTable());
        Object.Storage = new ArrayList<RakudoObject>();
        return Object;
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext TC, RakudoObject Obj)
    {
        return ((Instance)Obj).Storage != null;
    }

    public  RakudoObject get_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException();
    }

    public  RakudoObject get_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException();
    }

    public  void bind_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException();
    }

    public  void bind_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException();
    }

    public  int hint_for(ThreadContext TC, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException();
    }

    public  void set_int(ThreadContext TC, RakudoObject Object, int Value)
    {
        throw new UnsupportedOperationException();
    }

    public  int get_int(ThreadContext TC, RakudoObject Object)
    {
        throw new UnsupportedOperationException();
    }

    public  void set_num(ThreadContext TC, RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException();
    }

    public  double get_num(ThreadContext TC, RakudoObject Object)
    {
        throw new UnsupportedOperationException();
    }

    public  void set_str(ThreadContext TC, RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException();
    }

    public  String get_str(ThreadContext TC, RakudoObject Object)
    {
        throw new UnsupportedOperationException();
    }
}

