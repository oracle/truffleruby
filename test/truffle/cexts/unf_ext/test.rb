require 'unf_ext'

normalizer = UNF::Normalizer.new
str = 'garçon'

abort unless normalizer.normalize(str, :nfc).bytes == [103, 97, 114, 195, 167, 111, 110]
abort unless normalizer.normalize(str, :nfkc).bytes == [103, 97, 114, 195, 167, 111, 110]
abort unless normalizer.normalize(str, :nfd).bytes == [103, 97, 114, 99, 204, 167, 111, 110]
abort unless normalizer.normalize(str, :nfkd).bytes == [103, 97, 114, 99, 204, 167, 111, 110]
