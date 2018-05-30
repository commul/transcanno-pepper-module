# transcanno-pepper-module

This module includes TranscannoManipulator that transforms XML produced by Transc&Anno into a salt&pepper structure.

TranscannoManipulator flattens the XML structure of an input document and replaces all the annotations by spans. These spans inherit all the attributes of the annotations they derive from.

If 2 or more annotations have the same tagcode, they are merged into 1 span.
