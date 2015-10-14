'''

'''
import os

for root, subdir, files in os.walk('.'):
	for f in files:
		if '_IterativeTracking' in f:
			original = os.path.join(root, f)
			new = os.path.join(root, f.replace('_IterativeTracking.cfg', '_itertracking.properties').lower())
			with open(original, 'r') as fin:
				text = fin.read()
				segments = text.split('\n\n')
				with open(new, 'w') as fout:
					for seg in segments:
						lines = seg.split('\n')
						titles = lines[0].split('\t')
						data = lines[1].split('\t')
						for title, value in zip(titles, data):
							if title == 'event_type':
								title = 'eventType'
							if 'Range' in title:
								values = value.split(',')
								fout.write(title + 'LowEnd=' + values[0] + '\n')
								fout.write(title + 'HighEnd=' + values[1] + '\n')
							else:
								new_line = title + '=' + value + "\n"
								fout.write(new_line)
