# transcanno-pepper-module

This module includes TranscannoManipulator that transforms XML produced by Transc&Anno into a salt&pepper structure.

TranscannoManipulator flattens the XML structure of an input document and replaces all the annotations by spans. These spans inherit all the attributes of the annotations they derive from.

If 2 or more annotations have the same tagcode, they are merged into 1 span.

"tagcode" is the default name of the attribute that allows to merge 2 or more annotations into 1: if the value of "tagcode" of 2 or more annotations is the same, they are merged into 1 span.

However, it is possible to define another attribute that will allow to merge annotations when having the same value. It is defined via the AnnotationsUnifyingAttribute parameter.

For example, if 2 XML tags with the same value of "id" should be merged together, while launching the TranscannoManipulator, we will define:
AnnotationsUnifyingAttribute=id

Before processing tokenises the textual data.
