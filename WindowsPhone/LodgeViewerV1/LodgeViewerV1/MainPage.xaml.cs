namespace LodgeViewerV1
{
    using System;
    using System.Linq;

    using ESRI.ArcGIS.Client;
    using ESRI.ArcGIS.Client.Geometry;
    using ESRI.ArcGIS.Client.Toolkit.DataSources;

    public partial class MainPage 
    {
        // Constructor
        public MainPage()
        {
            InitializeComponent();
        }

        /// <summary>
        /// Handler to respond gestures in the map.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Map_OnMapGesture(object sender, Map.MapGestureEventArgs e)
        {
            if (e.Gesture == GestureType.Tap)
            {
                // Get layer that we are working with and get features that are inside of the click + 4 pixel radious
                var featureLayer = Map.Layers["Asunnot"] as FeatureLayer;
                var selected = e.DirectlyOver(4, new GraphicsLayer[] { featureLayer });

                foreach (var g in selected)
                {
                    // set anchor  - this is the place where the InfoWindow's needle is
                    this.LodgingInfoWindow.Anchor = g.Geometry as MapPoint;

                    // Open the InfoWindow
                    this.LodgingInfoWindow.IsOpen = true;

                    // Since a ContentTemplate is defined (in XAML), Content will define the DataContext for the ContentTemplate
                    this.LodgingInfoWindow.Content = g;
                    return;
                }

                // No results so close the info window
                if (!selected.Any())
                {
                    this.LodgingInfoWindow.IsOpen = false;
                }
            }
        }

        private void ApplicationBarIconButton_OnClick(object sender, EventArgs e)
        {
            this.ToC.IsOpen = !this.ToC.IsOpen;
        }

        private void OpenLegend(object sender, EventArgs e)
        {
            this.Legend.IsOpen = !this.Legend.IsOpen;
        }

        private void PanToGPS(object sender, EventArgs e)
        {
            this.Map.PanTo(this.Map.Layers.OfType<GpsLayer>().First().Position);
        }

        private void Layer_OnInitialized(object sender, EventArgs e)
        {
            // Since the map is using initial Extent from the world wide basemap, we want to zoom to relevant location by the extent of the Features used.
            this.Map.Extent = (sender as Layer).FullExtent;
        }
    }
}