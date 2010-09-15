#include "parrot.h"
#include "KnowHOW.h"
#include "../repr.h"

/* Sets up the KnowHOW representation and returns an instance
 * of it to work with. It is, in fact, a singleton. */
PMC * REPR_KnowHOW_setup(PARROT_INTERP) {
    
}

/* Makes a type object pointing to the given HOW. */
static PMC *type_object_for(PARROT_INTERP, PMC *self, PMC *HOW)
{
    STable *st = Rakudo_alloc_stable(INTVALerp);
    st->HOW = HOW;
    st->REPR = self;
    st->WHAT = Rakudo_alloc_object(INTVALerp, sizeof(KnowHOWInstance), st);
    return st->WHAT;
}

/* Create an instance of the given object. */
static PMC * instance_of(PARROT_INTERP, PMC *self, PMC *WHAT)
{
    PMC *obj = Rakudo_alloc_object(INTVALerp, sizeof(KnowHOWInstance),
        STableOf(WHAT));
    KnowHOWInstance *data = KnowHOWInstanceData(obj);
    data->Methods = pmc_new(INTVALerp, enum_class_Hash);
    data->Attributes = pmc_new(INTVALerp, enum_class_ResizablePMCArray);
    return obj;
}

/* Checks if the object is defined or not. */
static INTVALVAL defined(PARROT_INTERP, PMC *self, PMC *obj)
{
    return !PMC_IS_NULL(KnowHOWInstanceData(obj)->Methods);
}

PMC *get_attribute(PARROT_INTERP, PMC *self, PMC *Object, PMC *ClassHandle, STRING *Name)
{
    die("This type of representation does not hold attributes");
}

PMC *get_attribute_with_hint(PARROT_INTERP, PMC *self, PMC *Object, PMC *ClassHandle, STRING *Name, INTVAL HINTVAL)
{
    die("This type of representation does not hold attributes");
}

void bind_attribute(PARROT_INTERP, PMC *self, PMC *Object, PMC *ClassHandle, STRING *Name, PMC *Value)
{
    die("This type of representation does not hold attributes");
}

void bind_attribute_with_hint(PARROT_INTERP, PMC *self, PMC *Object, PMC *ClassHandle, STRING *Name, INTVAL HINTVAL, PMC *Value)
{
    die("This type of representation does not hold attributes");
}

INTVAL hint_for(PARROT_INTERP, PMC *self, PMC *ClassHandle, STRING *Name)
{
    die("This type of representation does not hold attributes");
}

void set_int(PARROT_INTERP, PMC *self, PMC *Object, INTVAL Value)
{
    die("This type of representation cannot box a native INTVAL");
}

INTVAL get_int(PARROT_INTERP, PMC *self, PMC *Object)
{
    die("This type of representation cannot unbox to a native INTVAL");
}

void set_num(PARROT_INTERP, PMC *self, PMC *Object, FLOATVAL Value)
{
    die("This type of representation cannot box a native num");
}

FLOATVAL get_num(PARROT_INTERP, PMC *self, PMC *Object)
{
    die("This type of representation cannot unbox to a native num");
}

void set_str(PARROT_INTERP, PMC *self, PMC *Object, STRING *Value)
{
    die("This type of representation cannot box a native string");
}

STRING *get_str(PARROT_INTERP, PMC *self, PMC *Object)
{
    die("This type of representation cannot unbox to a native string");
}
