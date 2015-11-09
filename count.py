import os

sum = 0
for data_folder in os.listdir("./data"):
    for data_file in os.listdir( "./data/"+ data_folder):
        file_count = 0
        with open("./data/"+ data_folder + "/" + data_file, 'r') as input_file:
            for line in input_file:
                if line.strip() != "":
                    file_count = file_count + 1
        print  "{}: {}".format(data_file, file_count)
        sum += file_count
print "Total: {}".format(sum)
