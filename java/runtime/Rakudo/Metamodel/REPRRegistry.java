package Rakudo.Metamodel;

import java.util.ArrayList;
import java.util.HashMap;

import Rakudo.Metamodel.Representation;

/// <summary>
/// This is our central registry of representations.
/// </summary>
public          class REPRRegistry
//public static class REPRRegistry // the C# version
{
    /// <summary>
    /// ID indexed list.
    /// </summary>
    private static ArrayList<Representation>
    Registry = new ArrayList<Representation>();
    // private static List<IRepresentation> Registry = new List<IRepresentation>(); // the C# version

    /// <summary>
    /// Maps names to IDs so we can do named lookups too.
    /// </summary>
    private static HashMap<String, Integer>
    NamedToIDMapper = new HashMap<String, Integer>();
    // private static Dictionary<String, int> NamedToIDMapper = new Dictionary<String, int>();

    /// <summary>
    /// Adds a representation to the registry and returns its
    /// registered ID.
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="REPR"></param>
    /// <returns></returns>
    public static int register_REPR(String name, Representation repr)
    {
        Registry.add(repr);
        int id = Registry.size() - 1;
        NamedToIDMapper.put(name, new Integer(id));
        return id;
    }

    /// <summary>
    /// Gets a representation by ID.
    /// </summary>
    /// <param name="ID"></param>
    /// <returns></returns>
    public static Representation get_REPR_by_id(int id)
    {
        return Registry.get( new Integer(id) );
    }

    /// <summary>
    /// Gets a representation by name.
    /// </summary>
    /// <param name="Name"></param>
    /// <returns></returns>
    public static Representation get_REPR_by_name(String name)
    {
        return Registry.get( NamedToIDMapper.get(name) );
    }
}

