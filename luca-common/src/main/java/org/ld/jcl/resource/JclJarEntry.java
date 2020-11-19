package org.ld.jcl.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JclJarEntry {
  private String baseUrl;
  private byte[] resourceBytes;
}
