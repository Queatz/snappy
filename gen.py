import os, re

# Removes server-side parts of the shared middleware

def copy(path):
	source = os.path.join('shared', path)
	target = os.path.join('app', path)
	
	for filename in os.listdir(source):
		sourcePath = os.path.join(source, filename)
		
		if os.path.isdir(sourcePath):
			copy(os.path.join(path, filename))
		else:
			proc(sourcePath, os.path.join(target, filename))

def proc(source, target):
	d = os.path.dirname(target)
	if not os.path.exists(d):
		os.makedirs(d)
	
	with open(source, 'r') as source, open(target, 'w+') as target:
		for line in source:
			if should(line):
				target.write(re.sub('@[A-Z]\w+', '', re.sub('@[A-Z]\w+\(.+\)', '', line)))
		

def should(line):
	return line.find('@Hide') == -1 and line.find('import com.goo') == -1 and line.find('import java.lang.annotation') == -1

copy('src/main/java/com/queatz/snappy/shared')
