meta:
  id: collision
  title: Collision file
  endian: le
  file-extension: col
seq:
  - id: col
    type: col
    repeat: eos
types:
  col:
    seq:
      - id: header
        type: col_header
      - id: padding
        size: header.size - 112
    instances:
      spheres:
        pos: header.sphereoffset + 4
        repeat: expr
        repeat-expr: header.numspheres
        type: col_sphere
        if: header.sphereoffset > 0
      boxes:
        pos: header.boxoffset + 4
        repeat: expr
        repeat-expr: header.numboxes
        type: col_box
        if: header.boxoffset > 0
      numfacegroups:
        pos: header.triangleoffset
        type: u4
        if: hasfacegroups
      facegroups:
        pos: header.triangleoffset - (28 * numfacegroups)
        type: col_facegroup
        repeat: expr
        repeat-expr: numfacegroups
        if: hasfacegroups
      vertices:
        pos: header.vertexoffset
        type: col_vertex
        repeat: until
        repeat-until: _io.pos + 6 >= vertexendpos
      vertexendpos:
        value: 'hasfacegroups ? header.triangleoffset - (28 * numfacegroups) : header.triangleoffset + 4'
      faces:
        pos: header.triangleoffset + 4
        type: col_face
        repeat: expr
        repeat-expr: header.numfaces
      shadowvertices:
        pos: header.shadowvertexoffset
        type: col_vertex
        repeat: until
        repeat-until: _io.pos + 6 >= header.shadowfaceoffset
        if: hasshadowmesh
      usecones:
        value: (header.flags & 1) + 0 != 0
      notempty:
        value: (header.flags & 2) + 0 != 0
      hasfacegroups:
        value: (header.flags & 8) + 0 != 0
      hasshadowmesh:
        value: (header.flags & 16) + 0 != 0    
  col_header:
    seq:
      - id: ident
        type: str
        encoding: ASCII
        size: 4
      - id: size
        type: u4
      - id: name
        type: str
        size: 22
        encoding: ASCII
      - id: modelid
        type: u2
      - id: bounds
        type: col_bounds
      - id: numspheres
        type: u2
      - id: numboxes
        type: u2
      - id: numfaces
        type: u2
      - id: numlines
        type: u1
      - id: unused
        type: u1
      - id: flags
        type: u4
      - id: sphereoffset
        type: u4
      - id: boxoffset
        type: u4
      - id: lineoffset
        type: u4
      - id: vertexoffset
        type: u4
      - id: triangleoffset
        type: u4
      - id: planeoffset
        type: u4
      - id: numshadowmeshfaces
        type: u4
      - id: shadowvertexoffset
        type: u4
      - id: shadowfaceoffset
        type: u4
  col_sphere:
    seq:
      - id: center
        type: col_vec3
      - id: radius
        type: f4
      - id: surface
        type: col_surface
  col_box:
    seq:
      - id: min
        type: col_vec3
      - id: max
        type: col_vec3
      - id: surface
        type: col_surface
  col_vec3:
    seq:
      - id: x
        type: f4
      - id: y
        type: f4
      - id: z
        type: f4
  col_surface:
    seq:
      - id: material
        type: u1
      - id: flag
        type: u1
      - id: brightness
        type: u1
      - id: light
        type: u1
  col_bounds:
    seq:
      - id: min
        type: col_vec3
      - id: max
        type: col_vec3
      - id: center
        type: col_vec3
      - id: radius
        type: f4
  col_vertex:
    seq:
      - id: vertex
        type: s2
        repeat: expr
        repeat-expr: 3
  col_facegroup:
    seq:
      - id: min
        type: col_vec3
      - id: max
        type: col_vec3
      - id: start_face
        type: u2
      - id: end_face
        type: u2
  col_face:
    seq:
      - id: a
        type: u2
      - id: b
        type: u2
      - id: c
        type: u2
      - id: material
        type: u1
      - id: light
        type: u1  
