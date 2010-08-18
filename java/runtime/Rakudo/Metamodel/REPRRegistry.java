package Rakudo.Metamodel;

import java.util.*; // ArrayList HashMap

/// <summary>
/// This is our central registry of representations.
/// </summary>
public          class REPRRegistry
//public static class REPRRegistry // the C# version
{
    /// <summary>
    /// ID indexed list.
    /// </summary>
    private static ArrayList<IRepresentation> Registry = new ArrayList<IRepresentation>();
//  private static      List<IRepresentation> Registry = new      List<IRepresentation>(); // the C# version

    /// <summary>
    /// Maps names to IDs so we can do named lookups too.
    /// </summary>
    private static HashMap<String, Integer> NamedToIDMapper = new HashMap<String, Integer>();
//  private static Dictionary<String, int> NamedToIDMapper = new Dictionary<String, int>();

    /// <summary>
    /// Adds a representation to the registry and returns its
    /// registered ID.
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="REPR"></param>
    /// <returns></returns>
    public static int register_REPR(String Name, IRepresentation REPR)
    {
        Registry.add(REPR);
        int ID = Registry.size() - 1;
        NamedToIDMapper.put(Name, new Integer(ID));
        return ID;
    }

    /// <summary>
    /// Gets a representation by ID.
    /// </summary>
    /// <param name="ID"></param>
    /// <returns></returns>
    public static IRepresentation get_REPR_by_id(int ID)
    {
        return Registry.get( new Integer(ID) );
    }

    /// <summary>
    /// Gets a representation by name.
    /// </summary>
    /// <param name="Name"></param>
    /// <returns></returns>
    public static IRepresentation get_REPR_by_name(String Name)
    {
        return Registry.get( NamedToIDMapper.get(Name) );
    }
}

