package Rakudo.Runtime;

import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;

/// <summary>
/// Represents a lexpad - either the static version or the dynamic
/// one.
/// </summary>
public class Lexpad  // struct in the C# version
{
    /// <summary>
    /// This is the slot mapping, allocating names to slots. All the
    /// dynamic variants of a lexpad will share this with the static
    /// lexpad.
    /// </summary>
    public HashMap<String, Integer> SlotMapping;

    /// <summary>
    /// The storage assocaited with the lexpad.
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
        int Slot = 0;
        for (String Name : SlotNames) {
            SlotMapping.put(Name, Slot++);
            // System.err.println("adding into LexPad: " + Name);
        }
        // System.err.println("LexPad SlotNames length: " + SlotNames.length);
        Storage = new RakudoObject[SlotNames.length];
    }

    /// <summary>
    /// Looks up a lexical by name.
    /// </summary>
    /// <param name="Name"></param>
    /// <returns></returns>
    public RakudoObject GetByName(String Name)
    {
        return Storage[SlotMapping.get(Name)];
    }

    /// <summary>
    /// Sets a lexical by name.
    /// </summary>
    /// <param name="Name"></param>
    /// <returns></returns>
    public RakudoObject SetByName(String Name, RakudoObject Value)
    {
        Storage[SlotMapping.get(Name)] = Value;
        return Value;
    }

    /// <summary>
    /// Extends the lexpad with an extra slot.  TODO: optimize if possible
    /// </summary>
    /// <param name="Name"></param>
    public void Extend(String[] Names)
    {
        // Add new entry to the mapping. Note that we re-build
        // it and make it unique to this lexpad now, even if it
        // was shared before, but add the extra entry.
        SlotMapping = new HashMap<String, Integer>(SlotMapping);
        int NewSlot = Storage.length;
        for (String Name : Names)
            SlotMapping.put(Name, NewSlot++);

        // Reallocate enlarged storage.
        RakudoObject[] NewStorage = new RakudoObject[Storage.length + Names.length];
        for (int i = 0; i < Storage.length; i++)
            NewStorage[i] = Storage[i];
        Storage = NewStorage;
    }
}

