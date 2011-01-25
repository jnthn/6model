package Rakudo.Runtime;

import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;




/// <summary>
/// Represents a lexpad - either the static version or the dynamic
/// one.
/// </summary>
public class Lexpad  // C# has struct
{
    /// <summary>
    /// This is the slot mapping, allocating names to slots. All the
    /// dynamic variants of a lexpad will share this with the static
    /// lexpad.
    /// </summary>
    public HashMap<String, Integer> SlotMapping; // C# has Dictionary<string, int>

    /// <summary>
    /// The storage associated with the lexpad.
    /// </summary>
    public RakudoObject[] Storage;

    /// <summary>
    /// Creates a new static lexpad with the given names all
    /// allocated slots, in the order they appear in the array.
    /// </summary>
    /// <param name="SlotNames"></param>
    public Lexpad(String[] SlotNames)
    {
        SlotMapping = new HashMap<String, Integer>(SlotNames.length);
        int slot = 0;
        for (String name : SlotNames)
            SlotMapping.put(name, slot++);
        Storage = new RakudoObject[SlotNames.length];
    }

    /// <summary>
    /// Looks up a lexical by name.
    /// </summary>
    /// <param name="Name"></param>
    /// <returns></returns>
    public RakudoObject GetByName(String name)
    {
        return Storage[SlotMapping.get(name)];
    }

    /// <summary>
    /// Sets a lexical by name.
    /// </summary>
    /// <param name="Name"></param>
    /// <returns></returns>
    public RakudoObject SetByName(String name, RakudoObject value)
    {
        Storage[SlotMapping.get(name)] = value;
        return value;
    }

    /// <summary>
    /// Extends the lexpad with an extra slot.
    /// </summary>
    /// <param name="Name"></param>
    public void Extend(String[] Names)
    {
        // Add new entry to the mapping. Note that we re-build
        // it and make it unique to this lexpad now, even if it
        // was shared before, but add the extra entry.
        SlotMapping = new HashMap<String, Integer>(SlotMapping);
        int NewSlot = Storage.length;
        for (String name : Names)
            SlotMapping.put(name, NewSlot++);

        // Reallocate enlarged storage.
        RakudoObject[] newStorage = new RakudoObject[Storage.length + Names.length];
        for (int i = 0; i < Storage.length; i++)
            newStorage[i] = Storage[i];
        Storage = newStorage;
    }
}

