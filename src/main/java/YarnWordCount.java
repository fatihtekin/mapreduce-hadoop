import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class YarnWordCount {
 
    public static class Map extends 
            Mapper<LongWritable, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, one);
            }
        }
    }

    public static class Reduce extends
            Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        conf.set("yarn.resourcemanager.address", "127.0.0.1:8032"); 
        conf.set("mapreduce.framework.name", "yarn");
        conf.set("fs.default.name", "hdfs://127.0.0.1:9000");
        conf.set("mapreduce.job.jar","C:\\Users\\hadoop\\projects\\wc-mvn\\target\\wc-mvn-0.0.1-SNAPSHOT.jar");
        
        Job job = Job.getInstance(conf, "WordCount");
        job.setJarByClass(YarnWordCount.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path("hdfs://127.0.0.1:9000/log.txt"));
        
        FileSystem fs = FileSystem.get(conf);
        Path out = new Path("hdfs://127.0.0.1:9000/out");
        fs.delete(out, true);
        
        FileOutputFormat.setOutputPath(job, new Path("hdfs://127.0.0.1:9000/out"));

        job.waitForCompletion(true);
    }

}