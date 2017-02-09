# This script transforms all the BIF networks into NET equivalents for their posterior analysis 
# using the Genie software.

#########################################################
### A) Installing and loading required packages
#########################################################

if (!require("bnlearn")) {
  install.packages("bnlearn", dependencies = TRUE)
  library(bnlearn)
}

if (!require("foreign")) {
  install.packages("foreign", dependencies = TRUE)
  library(foreign)
}

input_directory = "networks/input_BIF/"
output_directory = "networks/output_NET/"

for(fileName in list.files(input_directory)){
  write.net(
    fitted = read.bif(file = paste(input_directory, fileName, sep = "")),
    file = paste(output_directory, tools::file_path_sans_ext(fileName), ".net",  sep = ""))
}
